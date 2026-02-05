package com.vectordb.service;

import com.vectordb.dto.*;
import com.vectordb.model.ContentType;
import com.vectordb.model.VectorDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorService {

    private final EmbeddingService embeddingService;
    private final VespaService vespaService;

    public EmbeddingResponse storeEmbedding(EmbeddingRequest request) {
        log.info("Storing embedding for content type: {}", request.getContentType());

        try {
            ContentType contentType = request.getContentType() != null ? 
                    request.getContentType() : ContentType.TEXT;

            // Generate embedding based on content type
            List<Float> embedding = generateEmbedding(request.getContent(), contentType);

            // Create document
            String documentId = UUID.randomUUID().toString();
            VectorDocument document = VectorDocument.builder()
                    .id(documentId)
                    .content(request.getContent())
                    .contentType(contentType)
                    .embedding(embedding)
                    .category(request.getCategory())
                    .description(request.getDescription())
                    .mediaUrl(request.getMediaUrl())
                    .createdAt(Instant.now())
                    .build();

            // Store in Vespa
            boolean stored = vespaService.storeDocument(document);

            if (stored) {
                return EmbeddingResponse.builder()
                        .id(documentId)
                        .content(request.getContent())
                        .contentType(contentType)
                        .category(request.getCategory())
                        .message("Document stored successfully")
                        .success(true)
                        .build();
            } else {
                return EmbeddingResponse.builder()
                        .content(request.getContent())
                        .contentType(contentType)
                        .message("Failed to store document in Vespa")
                        .success(false)
                        .build();
            }

        } catch (Exception e) {
            log.error("Error storing embedding: {}", e.getMessage(), e);
            return EmbeddingResponse.builder()
                    .content(request.getContent())
                    .message("Error: " + e.getMessage())
                    .success(false)
                    .build();
        }
    }

    public SearchResponse searchSimilar(SearchRequest request) {
        log.info("Searching for similar content: {}", request.getQuery());

        try {
            // Generate embedding for query
            List<Float> queryEmbedding = embeddingService.generateTextEmbedding(request.getQuery());

            // Determine content type preference
            ContentType preferredType = request.getPreferredType();
            boolean includeMedia = request.isIncludeMedia();

            // Search in Vespa
            return vespaService.search(
                    queryEmbedding, 
                    preferredType, 
                    includeMedia, 
                    request.getLimit() > 0 ? request.getLimit() : 5
            );

        } catch (Exception e) {
            log.error("Error searching: {}", e.getMessage(), e);
            return SearchResponse.builder()
                    .results(List.of())
                    .totalHits(0)
                    .query(request.getQuery())
                    .searchTimeMs(0)
                    .build();
        }
    }

    private List<Float> generateEmbedding(String content, ContentType contentType) {
        return switch (contentType) {
            case TEXT -> embeddingService.generateTextEmbedding(content);
            case IMAGE -> embeddingService.generateImageEmbedding(content);
            case VIDEO -> embeddingService.generateVideoEmbedding(content);
        };
    }
}

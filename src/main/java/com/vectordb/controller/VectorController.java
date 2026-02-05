package com.vectordb.controller;

import com.vectordb.dto.*;
import com.vectordb.model.ContentType;
import com.vectordb.service.DataLoaderService;
import com.vectordb.service.VectorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/vectors")
@RequiredArgsConstructor
public class VectorController {

    private final VectorService vectorService;
    private final DataLoaderService dataLoaderService;

    /**
     * POST endpoint to store text content with embedding.
     */
    @PostMapping("/embed")
    public ResponseEntity<EmbeddingResponse> embedContent(@Valid @RequestBody EmbeddingRequest request) {
        log.info("Received embedding request for content type: {}", request.getContentType());
        
        if (request.getContentType() == null) {
            request.setContentType(ContentType.TEXT);
        }
        
        EmbeddingResponse response = vectorService.storeEmbedding(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * POST endpoint to store text content.
     */
    @PostMapping("/text")
    public ResponseEntity<EmbeddingResponse> embedText(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        String category = body.getOrDefault("category", "general");
        String description = body.getOrDefault("description", "");
        
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(
                    EmbeddingResponse.builder()
                            .message("Content is required")
                            .success(false)
                            .build()
            );
        }
        
        EmbeddingRequest request = EmbeddingRequest.builder()
                .content(content)
                .contentType(ContentType.TEXT)
                .category(category)
                .description(description)
                .build();
        
        EmbeddingResponse response = vectorService.storeEmbedding(request);
        return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.internalServerError().body(response);
    }

    /**
     * POST endpoint to store image content with metadata.
     */
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EmbeddingResponse> embedImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "category", defaultValue = "images") String category) {
        
        log.info("Received image upload: {}", file.getOriginalFilename());
        
        // In a real implementation, you would:
        // 1. Store the file to a storage service (S3, GCS, etc.)
        // 2. Generate image embeddings using a vision model
        // 3. Store the embedding with the media URL
        
        String content = description != null ? description : file.getOriginalFilename();
        String mediaUrl = "/media/images/" + file.getOriginalFilename();
        
        EmbeddingRequest request = EmbeddingRequest.builder()
                .content(content)
                .contentType(ContentType.IMAGE)
                .category(category)
                .description(description)
                .mediaUrl(mediaUrl)
                .build();
        
        EmbeddingResponse response = vectorService.storeEmbedding(request);
        return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.internalServerError().body(response);
    }

    /**
     * POST endpoint to store video content with metadata.
     */
    @PostMapping(value = "/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EmbeddingResponse> embedVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "category", defaultValue = "videos") String category) {
        
        log.info("Received video upload: {}", file.getOriginalFilename());
        
        // In a real implementation, you would:
        // 1. Store the file to a storage service
        // 2. Extract keyframes and generate embeddings
        // 3. Store the embedding with the media URL
        
        String content = description != null ? description : file.getOriginalFilename();
        String mediaUrl = "/media/videos/" + file.getOriginalFilename();
        
        EmbeddingRequest request = EmbeddingRequest.builder()
                .content(content)
                .contentType(ContentType.VIDEO)
                .category(category)
                .description(description)
                .mediaUrl(mediaUrl)
                .build();
        
        EmbeddingResponse response = vectorService.storeEmbedding(request);
        return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.internalServerError().body(response);
    }

    /**
     * GET endpoint for similarity search.
     * By default returns only TEXT results unless includeMedia=true or preferredType is specified.
     */
    @GetMapping("/search")
    public ResponseEntity<SearchResponse> searchSimilar(
            @RequestParam String query,
            @RequestParam(required = false) ContentType preferredType,
            @RequestParam(defaultValue = "false") boolean includeMedia,
            @RequestParam(defaultValue = "5") int limit) {
        
        log.info("Search request - query: {}, preferredType: {}, includeMedia: {}", 
                query, preferredType, includeMedia);
        
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .preferredType(preferredType)
                .includeMedia(includeMedia)
                .limit(limit)
                .build();
        
        SearchResponse response = vectorService.searchSimilar(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST endpoint for similarity search with body.
     */
    @PostMapping("/search")
    public ResponseEntity<SearchResponse> searchSimilarPost(@Valid @RequestBody SearchRequest request) {
        log.info("Search request - query: {}", request.getQuery());
        
        SearchResponse response = vectorService.searchSimilar(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST endpoint to load sample data (50 texts about animals and cities).
     */
    @PostMapping("/load-samples")
    public ResponseEntity<Map<String, Object>> loadSampleData() {
        log.info("Loading sample data");
        
        List<EmbeddingResponse> results = dataLoaderService.loadSampleData();
        
        long successCount = results.stream().filter(EmbeddingResponse::isSuccess).count();
        long failCount = results.size() - successCount;
        
        return ResponseEntity.ok(Map.of(
                "message", "Sample data loading completed",
                "totalDocuments", results.size(),
                "successCount", successCount,
                "failCount", failCount
        ));
    }
}

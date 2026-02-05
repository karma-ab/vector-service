package com.vectordb.service;

import com.vectordb.config.EmbeddingConfig;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final EmbeddingConfig embeddingConfig;
    private Random random;
    private MessageDigest digest;

    @PostConstruct
    public void init() {
        log.info("Initializing embedding service with dimension: {}", embeddingConfig.getDimension());
        this.random = new Random(42);
        try {
            this.digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
        log.info("Embedding service initialized successfully");
    }

    @PreDestroy
    public void cleanup() {
        log.info("Cleaning up embedding service resources");
    }

    /**
     * Generate embeddings for text content.
     * Uses a deterministic hash-based approach for consistent embeddings.
     */
    public List<Float> generateTextEmbedding(String text) {
        log.debug("Generating embedding for text: {}", text.substring(0, Math.min(50, text.length())));
        return generateDeterministicEmbedding(text, embeddingConfig.getDimension());
    }

    /**
     * Generate embeddings for image content (using description/path).
     */
    public List<Float> generateImageEmbedding(String imageDescription) {
        log.debug("Generating embedding for image: {}", imageDescription);
        return generateDeterministicEmbedding("IMAGE:" + imageDescription, embeddingConfig.getDimension());
    }

    /**
     * Generate embeddings for video content (using description/path).
     */
    public List<Float> generateVideoEmbedding(String videoDescription) {
        log.debug("Generating embedding for video: {}", videoDescription);
        return generateDeterministicEmbedding("VIDEO:" + videoDescription, embeddingConfig.getDimension());
    }

    /**
     * Generate deterministic embedding based on content hash.
     * This ensures same content always produces same embedding.
     */
    private List<Float> generateDeterministicEmbedding(String content, int dimension) {
        byte[] hash = digest.digest(content.toLowerCase().getBytes());
        Random seededRandom = new Random(bytesToLong(hash));
        
        List<Float> embedding = new ArrayList<>(dimension);
        double sumSquares = 0;
        
        for (int i = 0; i < dimension; i++) {
            float value = (float) seededRandom.nextGaussian();
            embedding.add(value);
            sumSquares += value * value;
        }
        
        // Normalize the embedding vector
        double norm = Math.sqrt(sumSquares);
        for (int i = 0; i < dimension; i++) {
            embedding.set(i, (float) (embedding.get(i) / norm));
        }
        
        return embedding;
    }

    private long bytesToLong(byte[] bytes) {
        long value = 0;
        for (int i = 0; i < Math.min(8, bytes.length); i++) {
            value = (value << 8) | (bytes[i] & 0xff);
        }
        return value;
    }

    public int getEmbeddingDimension() {
        return embeddingConfig.getDimension();
    }
}

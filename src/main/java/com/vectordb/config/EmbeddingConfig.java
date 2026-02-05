package com.vectordb.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "embedding")
public class EmbeddingConfig {
    private String model = "sentence-transformers/all-MiniLM-L6-v2";
    private int dimension = 384;
    private int batchSize = 32;
}

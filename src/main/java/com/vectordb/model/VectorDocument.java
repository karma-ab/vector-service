package com.vectordb.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorDocument {
    private String id;
    private String content;
    private ContentType contentType;
    private List<Float> embedding;
    private String category;
    private String mediaUrl;
    private Instant createdAt;
    private String description;
}

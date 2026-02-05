package com.vectordb.dto;

import com.vectordb.model.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingResponse {
    private String id;
    private String content;
    private ContentType contentType;
    private String category;
    private String message;
    private boolean success;
}

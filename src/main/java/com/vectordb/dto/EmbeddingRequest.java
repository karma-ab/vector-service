package com.vectordb.dto;

import com.vectordb.model.ContentType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingRequest {
    @NotBlank(message = "Content is required")
    private String content;
    
    private ContentType contentType;
    
    private String category;
    
    private String description;
    
    private String mediaUrl;
}

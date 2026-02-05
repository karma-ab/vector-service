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
public class SearchRequest {
    @NotBlank(message = "Query is required")
    private String query;
    
    private ContentType preferredType;
    
    @Builder.Default
    private int limit = 5;
    
    @Builder.Default
    private boolean includeMedia = false;
}

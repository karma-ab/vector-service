package com.vectordb.dto;

import com.vectordb.model.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    private List<SearchResult> results;
    private int totalHits;
    private String query;
    private long searchTimeMs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchResult {
        private String id;
        private String content;
        private ContentType contentType;
        private String category;
        private String description;
        private String mediaUrl;
        private double score;
    }
}

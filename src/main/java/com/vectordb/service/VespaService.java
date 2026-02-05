package com.vectordb.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vectordb.config.VespaConfig;
import com.vectordb.dto.SearchResponse;
import com.vectordb.model.ContentType;
import com.vectordb.model.VectorDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VespaService {

    private final VespaConfig vespaConfig;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;

    private static final String NAMESPACE = "vectordb";
    private static final String DOC_TYPE = "embedding";

    public boolean storeDocument(VectorDocument document) {
        String documentUrl = buildDocumentUrl(document.getId());
        log.debug("Storing document at: {}", documentUrl);

        try {
            ObjectNode fields = objectMapper.createObjectNode();
            fields.put("content", document.getContent());
            fields.put("content_type", document.getContentType().name());
            fields.put("category", document.getCategory() != null ? document.getCategory() : "");
            fields.put("description", document.getDescription() != null ? document.getDescription() : "");
            fields.put("media_url", document.getMediaUrl() != null ? document.getMediaUrl() : "");
            fields.put("created_at", document.getCreatedAt().toEpochMilli());

            // Add embedding as tensor
            ObjectNode embeddingTensor = objectMapper.createObjectNode();
            ArrayNode values = objectMapper.createArrayNode();
            for (Float value : document.getEmbedding()) {
                values.add(value);
            }
            embeddingTensor.set("values", values);
            fields.set("embedding", embeddingTensor);

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.set("fields", fields);

            HttpPost request = new HttpPost(documentUrl);
            request.setEntity(new StringEntity(
                    objectMapper.writeValueAsString(requestBody),
                    org.apache.hc.core5.http.ContentType.APPLICATION_JSON
            ));

            return httpClient.execute(request, response -> {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                log.debug("Vespa store response: {} - {}", statusCode, responseBody);
                return statusCode >= 200 && statusCode < 300;
            });

        } catch (IOException e) {
            log.error("Error storing document in Vespa: {}", e.getMessage(), e);
            return false;
        }
    }

    public SearchResponse search(List<Float> queryEmbedding, ContentType preferredType, 
                                  boolean includeMedia, int limit) {
        long startTime = System.currentTimeMillis();
        
        try {
            String yql = buildSearchYql(preferredType, includeMedia, limit);
            String tensorString = buildTensorString(queryEmbedding);
            
            String searchUrl = vespaConfig.getSearchEndpoint() + "?" +
                    "yql=" + URLEncoder.encode(yql, StandardCharsets.UTF_8) +
                    "&ranking=semantic" +
                    "&input.query(q)=" + URLEncoder.encode(tensorString, StandardCharsets.UTF_8);

            log.debug("Search URL: {}", searchUrl);

            HttpGet request = new HttpGet(searchUrl);

            return httpClient.execute(request, response -> {
                String responseBody = EntityUtils.toString(response.getEntity());
                log.debug("Search response: {}", responseBody);
                return parseSearchResponse(responseBody, queryEmbedding.toString(), startTime);
            });

        } catch (IOException e) {
            log.error("Error searching in Vespa: {}", e.getMessage(), e);
            return SearchResponse.builder()
                    .results(new ArrayList<>())
                    .totalHits(0)
                    .searchTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    private String buildSearchYql(ContentType preferredType, boolean includeMedia, int limit) {
        StringBuilder yql = new StringBuilder("select * from sources * where ");
        
        if (preferredType != null && !includeMedia) {
            yql.append("content_type contains '").append(preferredType.name()).append("' and ");
        } else if (!includeMedia) {
            yql.append("content_type contains 'TEXT' and ");
        }
        
        yql.append("{targetHits: ").append(limit).append("}nearestNeighbor(embedding, q)");
        yql.append(" limit ").append(limit);
        
        return yql.toString();
    }

    private String buildTensorString(List<Float> embedding) {
        String values = embedding.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        return "[" + values + "]";
    }

    private SearchResponse parseSearchResponse(String responseBody, String query, long startTime) {
        List<SearchResponse.SearchResult> results = new ArrayList<>();
        int totalHits = 0;

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode rootNode = root.path("root");
            
            if (rootNode.has("children")) {
                JsonNode children = rootNode.get("children");
                for (JsonNode hit : children) {
                    JsonNode fields = hit.path("fields");
                    
                    SearchResponse.SearchResult result = SearchResponse.SearchResult.builder()
                            .id(hit.path("id").asText())
                            .content(fields.path("content").asText())
                            .contentType(ContentType.valueOf(fields.path("content_type").asText("TEXT")))
                            .category(fields.path("category").asText())
                            .description(fields.path("description").asText())
                            .mediaUrl(fields.path("media_url").asText())
                            .score(hit.path("relevance").asDouble())
                            .build();
                    
                    results.add(result);
                }
                totalHits = rootNode.path("fields").path("totalCount").asInt(results.size());
            }
        } catch (Exception e) {
            log.error("Error parsing search response: {}", e.getMessage(), e);
        }

        return SearchResponse.builder()
                .results(results)
                .totalHits(totalHits)
                .query(query)
                .searchTimeMs(System.currentTimeMillis() - startTime)
                .build();
    }

    public boolean deleteDocument(String documentId) {
        String documentUrl = buildDocumentUrl(documentId);
        
        try {
            HttpDelete request = new HttpDelete(documentUrl);
            return httpClient.execute(request, response -> {
                int statusCode = response.getCode();
                return statusCode >= 200 && statusCode < 300;
            });
        } catch (IOException e) {
            log.error("Error deleting document from Vespa: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean checkHealth() {
        try {
            HttpGet request = new HttpGet(vespaConfig.getStatusEndpoint());
            return httpClient.execute(request, response -> {
                int statusCode = response.getCode();
                return statusCode == 200;
            });
        } catch (IOException e) {
            log.warn("Vespa health check failed: {}", e.getMessage());
            return false;
        }
    }

    public VespaStatus getStatus() {
        try {
            HttpGet request = new HttpGet(vespaConfig.getEndpoint() + "/state/v1/health");
            return httpClient.execute(request, response -> {
                int statusCode = response.getCode();
                String body = EntityUtils.toString(response.getEntity());
                
                VespaStatus status = new VespaStatus();
                status.setConnected(statusCode == 200);
                
                if (statusCode == 200) {
                    JsonNode root = objectMapper.readTree(body);
                    status.setStatus(root.path("status").path("code").asText("unknown"));
                    status.setMessage(root.path("status").path("message").asText(""));
                } else {
                    status.setStatus("unhealthy");
                    status.setMessage("HTTP " + statusCode);
                }
                
                return status;
            });
        } catch (IOException e) {
            VespaStatus status = new VespaStatus();
            status.setConnected(false);
            status.setStatus("unreachable");
            status.setMessage(e.getMessage());
            return status;
        }
    }

    private String buildDocumentUrl(String documentId) {
        return String.format("%s/%s/%s/docid/%s",
                vespaConfig.getDocumentEndpoint(),
                NAMESPACE,
                DOC_TYPE,
                documentId);
    }

    public static class VespaStatus {
        private boolean connected;
        private String status;
        private String message;

        public boolean isConnected() { return connected; }
        public void setConnected(boolean connected) { this.connected = connected; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}

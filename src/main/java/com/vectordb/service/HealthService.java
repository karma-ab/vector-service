package com.vectordb.service;

import com.vectordb.dto.HealthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthService {

    private final VespaService vespaService;
    private final EmbeddingService embeddingService;

    public HealthResponse getHealth() {
        Map<String, HealthResponse.ComponentHealth> components = new HashMap<>();

        // Check application health
        components.put("application", checkApplicationHealth());

        // Check Vespa health
        components.put("vespa", checkVespaHealth());

        // Check embedding service health
        components.put("embedding", checkEmbeddingHealth());

        // Determine overall status
        String overallStatus = components.values().stream()
                .allMatch(c -> "UP".equals(c.getStatus())) ? "UP" : "DOWN";

        return HealthResponse.builder()
                .status(overallStatus)
                .timestamp(Instant.now())
                .components(components)
                .build();
    }

    private HealthResponse.ComponentHealth checkApplicationHealth() {
        return HealthResponse.ComponentHealth.builder()
                .status("UP")
                .message("Application is running")
                .details(Map.of(
                        "javaVersion", System.getProperty("java.version"),
                        "availableProcessors", Runtime.getRuntime().availableProcessors(),
                        "freeMemory", Runtime.getRuntime().freeMemory(),
                        "maxMemory", Runtime.getRuntime().maxMemory()
                ))
                .build();
    }

    private HealthResponse.ComponentHealth checkVespaHealth() {
        try {
            VespaService.VespaStatus status = vespaService.getStatus();
            
            Map<String, Object> details = new HashMap<>();
            details.put("connected", status.isConnected());
            details.put("vespaStatus", status.getStatus());
            
            if (status.isConnected()) {
                return HealthResponse.ComponentHealth.builder()
                        .status("UP")
                        .message("Vespa is connected and healthy")
                        .details(details)
                        .build();
            } else {
                return HealthResponse.ComponentHealth.builder()
                        .status("DOWN")
                        .message("Vespa connection failed: " + status.getMessage())
                        .details(details)
                        .build();
            }
        } catch (Exception e) {
            log.error("Error checking Vespa health: {}", e.getMessage());
            return HealthResponse.ComponentHealth.builder()
                    .status("DOWN")
                    .message("Error: " + e.getMessage())
                    .details(Map.of("error", e.getClass().getSimpleName()))
                    .build();
        }
    }

    private HealthResponse.ComponentHealth checkEmbeddingHealth() {
        try {
            int dimension = embeddingService.getEmbeddingDimension();
            
            return HealthResponse.ComponentHealth.builder()
                    .status("UP")
                    .message("Embedding service is operational")
                    .details(Map.of(
                            "embeddingDimension", dimension,
                            "modelStatus", "ready"
                    ))
                    .build();
        } catch (Exception e) {
            log.error("Error checking embedding health: {}", e.getMessage());
            return HealthResponse.ComponentHealth.builder()
                    .status("DOWN")
                    .message("Error: " + e.getMessage())
                    .details(Map.of("error", e.getClass().getSimpleName()))
                    .build();
        }
    }
}

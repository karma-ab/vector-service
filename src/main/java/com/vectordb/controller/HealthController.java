package com.vectordb.controller;

import com.vectordb.dto.HealthResponse;
import com.vectordb.service.HealthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
public class HealthController {

    private final HealthService healthService;

    /**
     * Comprehensive health check endpoint for both DB and application.
     */
    @GetMapping
    public ResponseEntity<HealthResponse> health() {
        HealthResponse response = healthService.getHealth();
        
        if ("UP".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(503).body(response);
        }
    }

    /**
     * Simple liveness probe - just checks if application is running.
     */
    @GetMapping("/live")
    public ResponseEntity<Map<String, Object>> liveness() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "timestamp", Instant.now()
        ));
    }

    /**
     * Readiness probe - checks if application can serve requests.
     */
    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> readiness() {
        HealthResponse health = healthService.getHealth();
        
        // Application is ready if both app and vespa are up
        boolean isReady = health.getComponents().values().stream()
                .allMatch(c -> "UP".equals(c.getStatus()));
        
        Map<String, Object> response = Map.of(
                "status", isReady ? "UP" : "DOWN",
                "timestamp", Instant.now(),
                "components", health.getComponents().keySet()
        );
        
        if (isReady) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(503).body(response);
        }
    }

    /**
     * Database (Vespa) specific health check.
     */
    @GetMapping("/db")
    public ResponseEntity<HealthResponse.ComponentHealth> databaseHealth() {
        HealthResponse health = healthService.getHealth();
        HealthResponse.ComponentHealth vespaHealth = health.getComponents().get("vespa");
        
        if (vespaHealth != null && "UP".equals(vespaHealth.getStatus())) {
            return ResponseEntity.ok(vespaHealth);
        } else {
            return ResponseEntity.status(503).body(vespaHealth);
        }
    }
}

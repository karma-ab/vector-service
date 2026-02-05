package com.vectordb.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "vespa")
public class VespaConfig {
    private String endpoint = "http://localhost:8080";
    private String tenant = "default";
    private String application = "vector-app";
    private String instance = "default";
    private String certificatePath;
    private String privateKeyPath;
    private int connectionTimeout = 5000;
    private int readTimeout = 30000;
    
    public String getDocumentEndpoint() {
        return endpoint + "/document/v1";
    }
    
    public String getSearchEndpoint() {
        return endpoint + "/search/";
    }
    
    public String getStatusEndpoint() {
        return endpoint + "/status.html";
    }
}

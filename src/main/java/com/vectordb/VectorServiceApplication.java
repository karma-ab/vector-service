package com.vectordb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class VectorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VectorServiceApplication.class, args);
    }
}

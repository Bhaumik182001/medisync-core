package com.bhaumik18.medisync_core.provider.controller; // Adjust package as needed

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/v1/core/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Service is UP");
    }
}

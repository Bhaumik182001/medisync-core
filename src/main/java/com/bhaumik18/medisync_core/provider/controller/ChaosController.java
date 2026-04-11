package com.bhaumik18.medisync_core.provider.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/chaos")
public class ChaosController {

    // The Global Kill Switch State
    public static boolean isServiceDead = false;

    @PostMapping("/toggle-killswitch")
    public ResponseEntity<Boolean> toggleKillSwitch() {
        isServiceDead = !isServiceDead;
        if (isServiceDead) {
            log.error(">>> 🛑 CHAOS: CORE SERVICE IS NOW PLAYING DEAD (500) <<<");
        } else {
            log.info(">>> 🟢 CHAOS: CORE SERVICE RESTORED <<<");
        }
        return ResponseEntity.ok(isServiceDead);
    }

    // The endpoint the React Tracer hits to check if it should turn Green or Red
    @GetMapping("/health-check")
    public ResponseEntity<String> healthCheck() {
        if (isServiceDead) {
            throw new RuntimeException("CONNECTION REFUSED: Simulated Core Service Crash!");
        }
        return ResponseEntity.ok("HEALTHY");
    }
}

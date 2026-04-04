package com.bhaumik18.medisync_core.chaos;

import jakarta.servlet.*;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

// This class acts as BOTH the Control Panel (RestController) and the Latency Injector (Filter)
@Component
@RestController
@RequestMapping("/api/chaos")
public class NativeChaosMonkey implements Filter {

    private boolean isChaosActive = false;

    // 1. The Control Panel: Hit this endpoint to wake or sleep the monkey
    @PostMapping("/toggle")
    public String toggleChaos() {
        this.isChaosActive = !this.isChaosActive;
        System.out.println("🐒 CHAOS MONKEY STATE CHANGED. Active: " + this.isChaosActive);
        return "Chaos Monkey Active: " + this.isChaosActive;
    }

    // 2. The Interceptor: This intercepts EVERY request hitting the Core Service
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (isChaosActive) {
            try {
                // Generate a random delay between 1000ms (1s) and 5000ms (5s)
                int randomLatency = java.util.concurrent.ThreadLocalRandom.current().nextInt(1000, 5000);
                System.out.println("🐒 CHAOS MONKEY: Injecting " + randomLatency + "ms of latency...");
                Thread.sleep(randomLatency); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        chain.doFilter(request, response);
    }
}
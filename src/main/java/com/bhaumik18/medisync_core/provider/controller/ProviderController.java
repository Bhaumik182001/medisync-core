package com.bhaumik18.medisync_core.provider.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.bhaumik18.medisync_core.provider.dto.ProviderRequest;
import com.bhaumik18.medisync_core.provider.entity.Provider;
import com.bhaumik18.medisync_core.provider.service.ProviderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/providers")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;

    @PostMapping("/profile")
    public ResponseEntity<Provider> saveProfile(
            @AuthenticationPrincipal String identityEmail,
            @Valid @RequestBody ProviderRequest request
    ) {
        Provider savedProvider = providerService.createOrUpdateProvider(identityEmail, request);
        return ResponseEntity.ok(savedProvider);
    }
    
    @GetMapping("/profile")
    public ResponseEntity<?> getProviderProfile(Principal principal) {
        // 1. Spring Security automatically pulls the email from the JWT Subject
        String email = principal.getName(); 

        // 2. Query your database for this specific doctor's profile
        // Note: Update "providerService.findByEmail" to whatever your actual service/repo method is named!
        Optional<Provider> providerProfile = providerService.findByIdentityEmail(email);

        // 3. If found, return 200 OK with the data. If not, return 404 Not Found.
        if (providerProfile.isPresent()) {
            return ResponseEntity.ok(providerProfile.get());
        } else {
            return ResponseEntity.notFound().build(); // The frontend expects this 404 to trigger the creation form!
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Provider>> getAllProviders() {
        List<Provider> providers = providerService.getAllProviders();
        return ResponseEntity.ok(providers);
    }
}
package com.bhaumik18.medisync_core.provider.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.bhaumik18.medisync_core.provider.dto.ProviderRequest;
import com.bhaumik18.medisync_core.provider.entity.Provider;
import com.bhaumik18.medisync_core.provider.service.ProviderService;

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
}
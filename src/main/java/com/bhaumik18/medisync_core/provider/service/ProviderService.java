package com.bhaumik18.medisync_core.provider.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bhaumik18.medisync_core.provider.dto.ProviderRequest;
import com.bhaumik18.medisync_core.provider.entity.Provider;
import com.bhaumik18.medisync_core.provider.repository.ProviderRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProviderService {

    private final ProviderRepository providerRepository;

    @Transactional
    public Provider createOrUpdateProvider(String identityEmail, ProviderRequest request) {
        
        // 1. Check if the provider already exists using the Gatekeeper's email
        Optional<Provider> existingProvider = providerRepository.findByIdentityEmail(identityEmail);

        // 2. Either get the existing one, or create a new one
        Provider provider = existingProvider.orElseGet(Provider::new);

        // 3. Map the details
        provider.setIdentityEmail(identityEmail);
        provider.setFirstName(request.getFirstName());
        provider.setLastName(request.getLastName());
        provider.setSpecialization(request.getSpecialization());

        // 4. Save to PostgreSQL
        return providerRepository.save(provider);
    }
    
    public Optional<Provider> findByIdentityEmail(String email) {
        return providerRepository.findByIdentityEmail(email);
    }
    
    public List<Provider> getAllProviders() {
        return providerRepository.findAll();
    }
       
}
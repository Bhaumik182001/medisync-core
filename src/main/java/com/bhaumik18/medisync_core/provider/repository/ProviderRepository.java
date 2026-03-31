package com.bhaumik18.medisync_core.provider.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bhaumik18.medisync_core.provider.entity.Provider;

import java.util.Optional;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {
    
    // The bridge to Port 8081
    Optional<Provider> findByIdentityEmail(String identityEmail);
    
    boolean existsByIdentityEmail(String identityEmail);
}

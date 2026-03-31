package com.bhaumik18.medisync_core.provider.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bhaumik18.medisync_core.provider.entity.Schedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    
    List<Schedule> findByProviderId(Long providerId);
    
    Optional<Schedule> findByProviderIdAndScheduleDate(Long providerId, LocalDate scheduleDate);
}

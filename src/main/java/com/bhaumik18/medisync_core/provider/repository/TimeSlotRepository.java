package com.bhaumik18.medisync_core.provider.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bhaumik18.medisync_core.provider.entity.TimeSlot;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    
    List<TimeSlot> findByScheduleId(Long scheduleId);
    
    List<TimeSlot> findByScheduleIdAndIsBookedFalse(Long scheduleId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TimeSlot t WHERE t.id = :id")
    Optional<TimeSlot> findByIdWithPessimisticWriteLock(@Param("id") Long id);
}
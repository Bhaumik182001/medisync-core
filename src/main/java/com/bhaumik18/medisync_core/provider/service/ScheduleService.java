package com.bhaumik18.medisync_core.provider.service;

import com.bhaumik18.medisync_core.provider.dto.ScheduleRequest;
import com.bhaumik18.medisync_core.provider.entity.Provider;
import com.bhaumik18.medisync_core.provider.entity.Schedule;
import com.bhaumik18.medisync_core.provider.entity.TimeSlot;
import com.bhaumik18.medisync_core.provider.repository.ProviderRepository;
import com.bhaumik18.medisync_core.provider.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ProviderRepository providerRepository;
    private final ScheduleRepository scheduleRepository;

    @Transactional
    public Schedule generateSchedule(String identityEmail, ScheduleRequest request) {
        // 1. Verify the doctor actually exists in the Core database
        Provider provider = providerRepository.findByIdentityEmail(identityEmail)
                .orElseThrow(() -> new RuntimeException("Provider profile not found. Please complete profile first."));

        // 2. Prevent duplicate schedules for the exact same day
        Optional<Schedule> existingSchedule = scheduleRepository
                .findByProviderIdAndScheduleDate(provider.getId(), request.getScheduleDate());
        
        if (existingSchedule.isPresent()) {
            throw new RuntimeException("Schedule already exists for this date.");
        }

        // 3. Create the parent Schedule entity
        Schedule schedule = Schedule.builder()
                .provider(provider)
                .scheduleDate(request.getScheduleDate())
                .build();

        // 4. Algorithmically generate 30-minute TimeSlots
        LocalTime currentTime = request.getStartTime();
        while (currentTime.isBefore(request.getEndTime())) {
            TimeSlot slot = TimeSlot.builder()
                    .schedule(schedule)
                    .startTime(currentTime)
                    .endTime(currentTime.plusMinutes(30))
                    .isBooked(false)
                    .build();
            
            schedule.getTimeSlots().add(slot);
            currentTime = currentTime.plusMinutes(30); // Move forward 30 mins
        }

        // 5. Save the Schedule. Because we used CascadeType.ALL in the entity, 
        // Hibernate will automatically save all the TimeSlots for us!
        return scheduleRepository.save(schedule);
    }
    
    @Cacheable(value = "availableSlots", key = "#providerId")
    @Transactional(readOnly = true)
    public List<TimeSlot> getAvailableSlotsForProvider(Long providerId) {
        System.out.println(">>> FETCHING FROM POSTGRESQL... (If you see this, cache was missed/empty) <<<");
        
        // Find all schedules for this provider
        List<Schedule> schedules = scheduleRepository.findByProviderId(providerId);
        
        // Extract only the un-booked slots
        return schedules.stream()
                .flatMap(schedule -> schedule.getTimeSlots().stream())
                .filter(slot -> !slot.isBooked())
                .toList();
    }
}
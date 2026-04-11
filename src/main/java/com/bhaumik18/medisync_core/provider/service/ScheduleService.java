package com.bhaumik18.medisync_core.provider.service;

import com.bhaumik18.medisync_core.provider.dto.PatientAppointmentDTO;
import com.bhaumik18.medisync_core.provider.dto.ScheduleRequest;
import com.bhaumik18.medisync_core.provider.dto.TimeSlotResponse;
import com.bhaumik18.medisync_core.provider.entity.Provider;
import com.bhaumik18.medisync_core.provider.entity.Schedule;
import com.bhaumik18.medisync_core.provider.entity.TimeSlot;
import com.bhaumik18.medisync_core.provider.repository.ProviderRepository;
import com.bhaumik18.medisync_core.provider.repository.ScheduleRepository;
import com.bhaumik18.medisync_core.provider.repository.TimeSlotRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict; // 1. ADD THIS IMPORT

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ProviderRepository providerRepository;
    private final ScheduleRepository scheduleRepository;
    private final TimeSlotRepository timeSlotRepository;

    // 2. ADD CACHE EVICTION HERE
    // When this method runs, it wipes the "availableSlots" cache so the next GET hits the DB
    @CacheEvict(value = "availableSlots", allEntries = true)
    @Transactional
    public Schedule generateSchedule(String identityEmail, ScheduleRequest request) {
        Provider provider = providerRepository.findByIdentityEmail(identityEmail)
                .orElseThrow(() -> new RuntimeException("Provider profile not found. Please complete profile first."));

        Optional<Schedule> existingSchedule = scheduleRepository
                .findByProviderIdAndScheduleDate(provider.getId(), request.getScheduleDate());
        
        if (existingSchedule.isPresent()) {
            throw new RuntimeException("Schedule already exists for this date.");
        }

        // Determine the interval (Default to 30 if the frontend forgets to send it)
        int interval = (request.getIntervalMinutes() != null && request.getIntervalMinutes() > 0) 
                        ? request.getIntervalMinutes() 
                        : 30;

        Schedule schedule = Schedule.builder()
                .provider(provider)
                .scheduleDate(request.getScheduleDate())
                .build();

        LocalTime currentTime = request.getStartTime();
        
        // Loop dynamically based on the requested interval
        while (currentTime.plusMinutes(interval).isBefore(request.getEndTime()) || currentTime.plusMinutes(interval).equals(request.getEndTime())) {
            TimeSlot slot = TimeSlot.builder()
                    .schedule(schedule)
                    .startTime(currentTime)
                    .endTime(currentTime.plusMinutes(interval))
                    .isBooked(false)
                    .build();
            
            schedule.getTimeSlots().add(slot);
            currentTime = currentTime.plusMinutes(interval); // Move forward dynamically
        }

        return scheduleRepository.save(schedule);
    }
    
    @Cacheable(value = "availableSlots", key = "#providerId")
    @Transactional(readOnly = true)
    public List<TimeSlotResponse> getAvailableSlotsForProvider(Long providerId) {
        
        List<Schedule> schedules = scheduleRepository.findByProviderId(providerId);
        
        return schedules.stream()
                // The <TimeSlotResponse> below is the "nudge" the compiler needs
                .<TimeSlotResponse>flatMap(schedule -> schedule.getTimeSlots().stream()
                    .map(slot -> TimeSlotResponse.builder()
                        .id(slot.getId())
                        .startTime(slot.getStartTime())
                        .endTime(slot.getEndTime())
                        .isBooked(slot.isBooked())
                        .scheduleDate(schedule.getScheduleDate())
                        .build()
                    )
                )
                .toList();
    }
    
    @CacheEvict(value = "availableSlots", allEntries = true)
    @Transactional
    public void deleteTimeSlot(String identityEmail, Long slotId) {
        // 1. Find the provider
        Provider provider = providerRepository.findByIdentityEmail(identityEmail)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        // 2. Find the slot using YOUR repository
        TimeSlot slot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        // 3. Security Check
        if (!slot.getSchedule().getProvider().getId().equals(provider.getId())) {
            throw new RuntimeException("Unauthorized: This slot does not belong to you.");
        }

        // 4. Prevent deleting booked appointments
        if (slot.isBooked()) {
            throw new RuntimeException("Cannot delete a booked slot. Cancel the appointment first.");
        }

        timeSlotRepository.delete(slot);
    }

    @Transactional
    @CacheEvict(value = "availableSlots", allEntries = true)
    public void markSlotAsBooked(Long slotId, String patientEmail) {
        TimeSlot slot = timeSlotRepository.findById(slotId).orElseThrow();
        slot.setBooked(true);
        slot.setPatientEmail(patientEmail); // Save the owner!
        timeSlotRepository.save(slot);
    }

    @Transactional
    @CacheEvict(value = "availableSlots", allEntries = true)
    public void cancelBooking(Long slotId, String patientEmail) {
        TimeSlot slot = timeSlotRepository.findById(slotId).orElseThrow();
        
        // Security check: Only the owner can cancel
        if (!patientEmail.equals(slot.getPatientEmail())) {
            throw new RuntimeException("Unauthorized cancellation attempt.");
        }
        
        slot.setBooked(false);
        slot.setPatientEmail(null); // Free it up!
        timeSlotRepository.save(slot);
    }
    
    public List<PatientAppointmentDTO> getPatientAppointments(String email) {
        List<TimeSlot> slots = timeSlotRepository.findByPatientEmailAndIsBookedTrue(email);
        
        return slots.stream().map(slot -> new PatientAppointmentDTO(
                slot.getId(),
                slot.getSchedule().getScheduleDate().toString(),
                slot.getStartTime().toString(),
                "Dr. " + slot.getSchedule().getProvider().getLastName(),
                slot.getSchedule().getProvider().getSpecialization()
        )).collect(Collectors.toList());
    }
}
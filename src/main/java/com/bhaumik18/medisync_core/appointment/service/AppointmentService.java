package com.bhaumik18.medisync_core.appointment.service;

import com.bhaumik18.medisync_core.appointment.entity.Appointment;
import com.bhaumik18.medisync_core.appointment.repository.AppointmentRepository;
import com.bhaumik18.medisync_core.provider.entity.TimeSlot;
import com.bhaumik18.medisync_core.provider.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final TimeSlotRepository timeSlotRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional
    @CacheEvict(value = "availableSlots", key = "#result.timeSlot.schedule.provider.id")
    public Appointment bookAppointment(String patientEmail, Long timeSlotId) {
        
        // 1. Fetch the slot AND LOCK THE DATABASE ROW. 
        // Any other thread trying to book this exact ID will pause right here.
        TimeSlot slot = timeSlotRepository.findByIdWithPessimisticWriteLock(timeSlotId)
                .orElseThrow(() -> new RuntimeException("Time slot not found"));

        // 2. The thread that gets the lock first checks if it's already gone
        if (slot.isBooked()) {
            throw new RuntimeException("CRITICAL: Time slot is already booked.");
        }

        // 3. Claim the slot
        slot.setBooked(true);
        timeSlotRepository.save(slot); // Update the slot to booked

        // 4. Generate the Appointment receipt
        Appointment appointment = Appointment.builder()
                .patientEmail(patientEmail)
                .timeSlot(slot)
                .bookingTime(LocalDateTime.now())
                .status("CONFIRMED")
                .build();

        return appointmentRepository.save(appointment);
    }
    
    @Transactional
    // THE FIX 1: We use allEntries=true because if we return null below, the old SpEL key (#result...) would crash!
    @CacheEvict(value = "availableSlots", allEntries = true) 
    public Appointment cancelAppointment(Long timeSlotId) {
        
        // 1. ALWAYS unlock the TimeSlot first. The Orchestrator definitely locked this in Step 1.
        TimeSlot slot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new RuntimeException("TimeSlot not found during rollback!"));
        
        slot.setBooked(false);
        slot.setPatientEmail(null); // Clear the owner so it's fully available again
        timeSlotRepository.save(slot);
        
        // 2. Safely check if the Appointment was ever actually created in Step 2
        Optional<Appointment> appointmentOpt = appointmentRepository.findByTimeSlotId(timeSlotId);
        
        if (appointmentOpt.isPresent()) {
            Appointment appointment = appointmentOpt.get();
            appointmentRepository.delete(appointment);
            System.out.println(">>> COMPENSATING ACTION: Appointment deleted and TimeSlot " + timeSlotId + " is freed. <<<");
            return appointment;
        } else {
            System.out.println(">>> COMPENSATING ACTION: TimeSlot " + timeSlotId + " freed. (Appointment never existed, skipping delete). <<<");
            return null; // Safe to return null during a rollback
        }
    }
    
    @org.springframework.transaction.annotation.Transactional
    public String resetDatabaseForTesting() {
        // Delete all bookings
        appointmentRepository.deleteAll();
        // Free up the schedule
        timeSlotRepository.releaseAllSlots();
        return "DEV MODE: All appointments wiped. Schedule is completely open.";
    }
}
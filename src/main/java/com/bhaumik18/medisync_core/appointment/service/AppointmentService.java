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
    @CacheEvict(value = "availableSlots", key = "#result.timeSlot.schedule.provider.id")
    public Appointment cancelAppointment(Long timeSlotId) {
    	Appointment appointment = appointmentRepository.findByTimeSlotId(timeSlotId)
    			.orElseThrow(() -> new RuntimeException("No appointment found for this slot."));
    	
    	TimeSlot slot = appointment.getTimeSlot();
    	
    	slot.setBooked(false);
    	timeSlotRepository.save(slot);
    	
    	appointmentRepository.delete(appointment);

        System.out.println(">>> COMPENSATING ACTION: Appointment deleted and TimeSlot " + timeSlotId + " is freed. <<<");

        return appointment;
    }
}
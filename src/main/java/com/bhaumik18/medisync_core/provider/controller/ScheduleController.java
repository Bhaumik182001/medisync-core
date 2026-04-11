package com.bhaumik18.medisync_core.provider.controller;

import com.bhaumik18.medisync_core.provider.dto.PatientAppointmentDTO;
import com.bhaumik18.medisync_core.provider.dto.ScheduleRequest;
import com.bhaumik18.medisync_core.provider.dto.TimeSlotResponse;
import com.bhaumik18.medisync_core.provider.entity.Schedule;
import com.bhaumik18.medisync_core.provider.entity.TimeSlot;
import com.bhaumik18.medisync_core.provider.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping("/generate")
    public ResponseEntity<Schedule> createSchedule(
            @AuthenticationPrincipal String identityEmail,
            @Valid @RequestBody ScheduleRequest request
    ) {
        Schedule generatedSchedule = scheduleService.generateSchedule(identityEmail, request);
        return ResponseEntity.ok(generatedSchedule);
    }
    
    @GetMapping("/provider/{providerId}/available")
    public ResponseEntity<List<TimeSlotResponse>> getAvailableSlots(@PathVariable Long providerId){
    	List<TimeSlotResponse> avaiableSlot = scheduleService.getAvailableSlotsForProvider(providerId);
    	
    	return ResponseEntity.ok(avaiableSlot);
    }
    
    @DeleteMapping("/slots/{slotId}")
    public ResponseEntity<?> deleteSlot(Principal principal, @PathVariable Long slotId) {
        scheduleService.deleteTimeSlot(principal.getName(), slotId);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/slots/{slotId}/book")
    public ResponseEntity<Void> markSlotAsBooked(
            @PathVariable Long slotId,
            @AuthenticationPrincipal String patientEmail // Spring Security automatically extracts this from the forwarded JWT!
    ) {
        scheduleService.markSlotAsBooked(slotId, patientEmail); 
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/slots/{slotId}/cancel")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable Long slotId,
            @AuthenticationPrincipal String patientEmail // Extracts JWT automatically
    ) {
        scheduleService.cancelBooking(slotId, patientEmail); 
        return ResponseEntity.ok().build();
    }
    
 // Don't forget this import if you don't have it!
    // import java.security.Principal;

    @GetMapping("/appointments/me")
    public ResponseEntity<List<PatientAppointmentDTO>> getMyAppointments(Principal principal) {
        String patientEmail = principal.getName();
        List<PatientAppointmentDTO> mySlots = scheduleService.getPatientAppointments(patientEmail);
        return ResponseEntity.ok(mySlots);
    }
} 
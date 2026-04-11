package com.bhaumik18.medisync_core.appointment.controller;

import com.bhaumik18.medisync_core.appointment.entity.Appointment;
import com.bhaumik18.medisync_core.appointment.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {
	
	private final AppointmentService appointmentService;
	
	@PostMapping("/book")
	public ResponseEntity<Appointment> bookAppointment(
			@AuthenticationPrincipal String patientEmail,
			@RequestBody Map<String, Long> payload
	){
		System.out.println(">>> REACHED CORE CONTROLLER! Patient: " + patientEmail + ", Slot: " + payload.get("timeSlotId"));
		Long timeSlotId = payload.get("timeSlotId");
		Appointment appointment = appointmentService.bookAppointment(patientEmail, timeSlotId);
		return ResponseEntity.ok(appointment);
	}
	
	@DeleteMapping("/cancel/{timeSlotId}")
	public ResponseEntity<String> cancelAppointment(
			@AuthenticationPrincipal String patientEmail,
			@PathVariable Long timeSlotId
	){
		appointmentService.cancelAppointment(timeSlotId);
		return ResponseEntity.ok("Successfully cancelled appointment and freed the time slot.");
	}
	
	@DeleteMapping("/reset-dev-data")
    public ResponseEntity<String> resetData() {
        return ResponseEntity.ok(appointmentService.resetDatabaseForTesting());
    }
	
}

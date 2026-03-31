package com.bhaumik18.medisync_core.provider.controller;

import com.bhaumik18.medisync_core.provider.dto.ScheduleRequest;
import com.bhaumik18.medisync_core.provider.entity.Schedule;
import com.bhaumik18.medisync_core.provider.entity.TimeSlot;
import com.bhaumik18.medisync_core.provider.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
    public ResponseEntity<List<TimeSlot>> getAvailableSlots(@PathVariable Long providerId){
    	List<TimeSlot> avaiableSlot = scheduleService.getAvailableSlotsForProvider(providerId);
    	
    	return ResponseEntity.ok(avaiableSlot);
    }
} 
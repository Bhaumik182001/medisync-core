package com.bhaumik18.medisync_core.provider.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlotResponse implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Long id; // Use String if your IDs are UUIDs
    private LocalTime startTime;
    private LocalTime endTime;
    
    @JsonProperty("isBooked")
    private boolean isBooked;
    
    // The crucial missing link!
    private LocalDate scheduleDate; 
}
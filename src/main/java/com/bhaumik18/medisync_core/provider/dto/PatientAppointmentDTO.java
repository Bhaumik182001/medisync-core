package com.bhaumik18.medisync_core.provider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PatientAppointmentDTO {
    private Long slotId;
    private String scheduleDate;
    private String startTime;
    private String providerName;
    private String specialization;
}
package com.bhaumik18.medisync_core.appointment.entity;

import com.bhaumik18.medisync_core.provider.entity.TimeSlot;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The Patient's Gatekeeper Email
    @Column(nullable = false)
    private String patientEmail;

    // The exact slot they booked
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_id", nullable = false, unique = true)
    private TimeSlot timeSlot;

    @Column(nullable = false)
    private LocalDateTime bookingTime;

    @Column(nullable = false)
    private String status; // "CONFIRMED", "CANCELLED"
}
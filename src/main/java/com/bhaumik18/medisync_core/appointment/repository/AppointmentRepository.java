package com.bhaumik18.medisync_core.appointment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bhaumik18.medisync_core.appointment.entity.Appointment;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

	Optional<Appointment> findByTimeSlotId(Long timeSlotId);
}

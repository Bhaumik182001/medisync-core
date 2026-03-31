package com.bhaumik18.medisync_core.appointment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bhaumik18.medisync_core.appointment.entity.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

}

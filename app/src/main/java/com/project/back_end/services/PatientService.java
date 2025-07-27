package com.example.service;

import com.example.dto.AppointmentDTO;
import com.example.model.Appointment;
import com.example.model.Patient;
import com.example.repository.AppointmentRepository;
import com.example.repository.PatientRepository;
import com.example.security.TokenService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    @Autowired
    public PatientService(PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    // ✅ Create a patient
    public int createPatient(Patient patient) {
        try {
            patientRepository.save(patient);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // ✅ Fetch appointments for patient by ID and validate token
    public ResponseEntity<Map<String, Object>> getPatientAppointment(Long id, String token) {
        Map<String, Object> response = new HashMap<>();
        String email = tokenService.extractEmail(token);
        Patient patient = patientRepository.findByEmail(email);

        if (patient == null || !Objects.equals(patient.getId(), id)) {
            response.put("message", "Unauthorized access.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        List<AppointmentDTO> dtos = appointmentRepository.findByPatientId(id).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        response.put("appointments", dtos);
        return ResponseEntity.ok(response);
    }

    // ✅ Filter by condition (past or future)
    public ResponseEntity<Map<String, Object>> filterByCondition(String condition, Long id) {
        Map<String, Object> response = new HashMap<>();
        int status = condition.equalsIgnoreCase("past") ? 1 : 0;

        List<AppointmentDTO> dtos = appointmentRepository
                .findByPatient_IdAndStatusOrderByAppointmentTimeAsc(id, status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        response.put("appointments", dtos);
        return ResponseEntity.ok(response);
    }

    // ✅ Filter appointments by doctor's name
    public ResponseEntity<Map<String, Object>> filterByDoctor(String name, Long patientId) {
        Map<String, Object> response = new HashMap<>();

        List<AppointmentDTO> dtos = appointmentRepository
                .filterByDoctorNameAndPatientId(name, patientId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        response.put("appointments", dtos);
        return ResponseEntity.ok(response);
    }

    // ✅ Filter appointments by doctor and condition
    public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(String condition, String name, long patientId) {
        Map<String, Object> response = new HashMap<>();
        int status = condition.equalsIgnoreCase("past") ? 1 : 0;

        List<AppointmentDTO> dtos = appointmentRepository
                .filterByDoctorNameAndPatientIdAndStatus(name, patientId, status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        response.put("appointments", dtos);
        return ResponseEntity.ok(response);
    }

    // ✅ Get patient details using token
    public ResponseEntity<Map<String, Object>> getPatientDetails(String token) {
        Map<String, Object> response = new HashMap<>();
        String email = tokenService.extractEmail(token);
        Patient patient = patientRepository.findByEmail(email);

        if (patient == null) {
            response.put("message", "Patient not found.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        response.put("patient", patient);
        return ResponseEntity.ok(response);
    }

    // ✅ Helper: Convert Appointment → AppointmentDTO
    private AppointmentDTO convertToDTO(Appointment appointment) {
        return new AppointmentDTO(
                appointment.getId(),
                appointment.getDoctor().getId(),
                appointment.getDoctor().getName(),
                appointment.getPatient().getId(),
                appointment.getPatient().getName(),
                appointment.getPatient().getEmail(),
                appointment.getPatient().getPhone(),
                appointment.getPatient().getAddress(),
                appointment.getAppointmentTime(),
                appointment.getStatus()
        );
    }
}

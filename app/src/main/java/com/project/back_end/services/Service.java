package com.example.service;

import com.example.dto.Login;
import com.example.model.Admin;
import com.example.model.Appointment;
import com.example.model.Doctor;
import com.example.model.Patient;
import com.example.repository.AdminRepository;
import com.example.repository.DoctorRepository;
import com.example.repository.PatientRepository;
import com.example.security.TokenService;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class Service {

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public Service(TokenService tokenService,
                   AdminRepository adminRepository,
                   DoctorRepository doctorRepository,
                   PatientRepository patientRepository,
                   DoctorService doctorService,
                   PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    // ✅ Validate token for a given user
    public ResponseEntity<Map<String, String>> validateToken(String token, String user) {
        Map<String, String> response = new HashMap<>();
        boolean valid = tokenService.validateToken(token, user);
        if (!valid) {
            response.put("message", "Token is invalid or expired");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        response.put("message", "Token is valid");
        return ResponseEntity.ok(response);
    }

    // ✅ Validate admin credentials
    public ResponseEntity<Map<String, String>> validateAdmin(Admin receivedAdmin) {
        Map<String, String> response = new HashMap<>();
        Admin admin = adminRepository.findByUsername(receivedAdmin.getUsername());

        if (admin == null || !admin.getPassword().equals(receivedAdmin.getPassword())) {
            response.put("message", "Invalid credentials");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        String token = tokenService.generateToken(admin.getId());
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    // ✅ Doctor filtering by name, specialty, and time
    public Map<String, Object> filterDoctor(String name, String specialty, String time) {
        if (name != null && specialty != null && time != null)
            return doctorService.filterDoctorsByNameSpecilityandTime(name, specialty, time);
        else if (name != null && time != null)
            return doctorService.filterDoctorByNameAndTime(name, time);
        else if (name != null && specialty != null)
            return doctorService.filterDoctorByNameAndSpecility(name, specialty);
        else if (specialty != null && time != null)
            return doctorService.filterDoctorByTimeAndSpecility(specialty, time);
        else if (specialty != null)
            return doctorService.filterDoctorBySpecility(specialty);
        else if (time != null)
            return doctorService.filterDoctorsByTime(time);
        else
            return Map.of("doctors", doctorService.getDoctors());
    }

    // ✅ Validate if doctor is available for an appointment
    public int validateAppointment(Appointment appointment) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(appointment.getDoctor().getId());
        if (doctorOpt.isEmpty()) return -1;

        List<String> available = doctorService.getDoctorAvailability(doctorOpt.get().getId(), appointment.getAppointmentTime().toLocalDate());
        String requestedTime = appointment.getAppointmentTime().toLocalTime().toString();
        return available.contains(requestedTime) ? 1 : 0;
    }

    // ✅ Check if patient already exists by email or phone
    public boolean validatePatient(Patient patient) {
        Patient existing = patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone());
        return existing == null;
    }

    // ✅ Validate patient login credentials
    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {
        Map<String, String> response = new HashMap<>();
        Patient patient = patientRepository.findByEmail(login.getIdentifier());

        if (patient == null || !patient.getPassword().equals(login.getPassword())) {
            response.put("message", "Invalid credentials");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        String token = tokenService.generateToken(patient.getId());
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    // ✅ Filter patient appointments based on condition and/or doctor name
    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String name, String token) {
        if (condition != null && name != null) {
            return patientService.filterByDoctorAndCondition(condition, name, tokenService.extractUserId(token));
        } else if (condition != null) {
            return patientService.filterByCondition(condition, tokenService.extractUserId(token));
        } else if (name != null) {
            return patientService.filterByDoctor(name, tokenService.extractUserId(token));
        } else {
            return patientService.getPatientAppointment(tokenService.extractUserId(token), token);
        }
    }
}

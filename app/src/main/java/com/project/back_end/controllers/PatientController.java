package com.example.controller;

import com.example.model.Login;
import com.example.model.Patient;
import com.example.service.PatientService;
import com.example.service.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/patient")
public class PatientController {

    private final PatientService patientService;
    private final Service service;

    @Autowired
    public PatientController(PatientService patientService, Service service) {
        this.patientService = patientService;
        this.service = service;
    }

    // ✅ 1. Get Patient Details
    @GetMapping("/{token}")
    public ResponseEntity<?> getPatientDetails(@PathVariable String token) {
        ResponseEntity<Map<String, String>> validationResponse = service.validateToken(token, "patient");
        if (validationResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(Map.of("message", "Unauthorized access"), HttpStatus.UNAUTHORIZED);
        }

        return ResponseEntity.ok(patientService.getPatientDetails(token));
    }

    // ✅ 2. Create a New Patient
    @PostMapping
    public ResponseEntity<Map<String, String>> createPatient(@RequestBody Patient patient) {
        int result = patientService.createPatient(patient);

        return switch (result) {
            case 1 -> new ResponseEntity<>(Map.of("message", "Signup successful"), HttpStatus.CREATED);
            case -1 -> new ResponseEntity<>(Map.of("message", "Patient with email id or phone no already exist"), HttpStatus.CONFLICT);
            default -> new ResponseEntity<>(Map.of("message", "Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
        };
    }

    // ✅ 3. Patient Login
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> patientLogin(@RequestBody Login login) {
        return service.validatePatientLogin(login);
    }

    // ✅ 4. Get Patient Appointments
    @GetMapping("/{id}/{token}")
    public ResponseEntity<?> getPatientAppointments(
            @PathVariable Long id,
            @PathVariable String token) {

        ResponseEntity<Map<String, String>> validationResponse = service.validateToken(token, "patient");
        if (validationResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(Map.of("message", "Unauthorized access"), HttpStatus.UNAUTHORIZED);
        }

        List<Map<String, Object>> appointments = patientService.getPatientAppointment(id);
        return ResponseEntity.ok(appointments);
    }

    // ✅ 5. Filter Patient Appointments
    @GetMapping("/filter/{condition}/{name}/{token}")
    public ResponseEntity<?> filterPatientAppointments(
            @PathVariable String condition,
            @PathVariable String name,
            @PathVariable String token) {

        ResponseEntity<Map<String, String>> validationResponse = service.validateToken(token, "patient");
        if (validationResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(Map.of("message", "Unauthorized access"), HttpStatus.UNAUTHORIZED);
        }

        return ResponseEntity.ok(service.filterPatient(condition, name, token));
    }
}

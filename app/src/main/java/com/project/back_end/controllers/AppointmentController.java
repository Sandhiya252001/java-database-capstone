package com.example.controller;

import com.example.model.Appointment;
import com.example.service.AppointmentService;
import com.example.service.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final Service service;

    @Autowired
    public AppointmentController(AppointmentService appointmentService, Service service) {
        this.appointmentService = appointmentService;
        this.service = service;
    }

    // ✅ Get appointments for a doctor (with token validation)
    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<Map<String, Object>> getAppointments(
            @PathVariable String date,
            @PathVariable String patientName,
            @PathVariable String token) {

        ResponseEntity<Map<String, String>> validationResponse = service.validateToken(token, "doctor");
        if (validationResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(Map.of("message", "Unauthorized access"), HttpStatus.UNAUTHORIZED);
        }

        LocalDate localDate = LocalDate.parse(date);
        return ResponseEntity.ok(appointmentService.getAppointment(patientName, localDate, token));
    }

    // ✅ Book appointment (for patients only)
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> bookAppointment(
            @PathVariable String token,
            @RequestBody Appointment appointment) {

        ResponseEntity<Map<String, String>> validationResponse = service.validateToken(token, "patient");
        if (validationResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(Map.of("message", "Unauthorized access"), HttpStatus.UNAUTHORIZED);
        }

        int validTime = service.validateAppointment(appointment);
        if (validTime == -1) return new ResponseEntity<>(Map.of("message", "Doctor not found"), HttpStatus.NOT_FOUND);
        if (validTime == 0) return new ResponseEntity<>(Map.of("message", "Selected time not available"), HttpStatus.BAD_REQUEST);

        int booked = appointmentService.bookAppointment(appointment);
        if (booked == 1) {
            return new ResponseEntity<>(Map.of("message", "Appointment booked successfully"), HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(Map.of("message", "Error booking appointment"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ✅ Update appointment (for authenticated patients)
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateAppointment(
            @PathVariable String token,
            @RequestBody Appointment appointment) {

        ResponseEntity<Map<String, String>> validationResponse = service.validateToken(token, "patient");
        if (validationResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(Map.of("message", "Unauthorized access"), HttpStatus.UNAUTHORIZED);
        }

        return appointmentService.updateAppointment(appointment);
    }

    // ✅ Cancel appointment (for the booking patient only)
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> cancelAppointment(
            @PathVariable Long id,
            @PathVariable String token) {

        ResponseEntity<Map<String, String>> validationResponse = service.validateToken(token, "patient");
        if (validationResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(Map.of("message", "Unauthorized access"), HttpStatus.UNAUTHORIZED);
        }

        return appointmentService.cancelAppointment(id, token);
    }
}

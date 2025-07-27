package com.example.controller;

import com.example.model.Prescription;
import com.example.service.PrescriptionService;
import com.example.service.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("${api.path}prescription")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final Service service;

    @Autowired
    public PrescriptionController(PrescriptionService prescriptionService, Service service) {
        this.prescriptionService = prescriptionService;
        this.service = service;
    }

    // ✅ 1. Save Prescription
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> savePrescription(
            @PathVariable String token,
            @RequestBody Prescription prescription) {

        ResponseEntity<Map<String, String>> validationResponse = service.validateToken(token, "doctor");
        if (validationResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(Map.of("message", "Unauthorized access"), HttpStatus.UNAUTHORIZED);
        }

        int result = prescriptionService.savePrescription(prescription);
        return switch (result) {
            case 1 -> ResponseEntity.ok(Map.of("message", "Prescription saved successfully"));
            default -> new ResponseEntity<>(Map.of("message", "Internal error while saving prescription"), HttpStatus.INTERNAL_SERVER_ERROR);
        };
    }

    // ✅ 2. Get Prescription by Appointment ID
    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<?> getPrescription(
            @PathVariable Long appointmentId,
            @PathVariable String token) {

        ResponseEntity<Map<String, String>> validationResponse = service.validateToken(token, "doctor");
        if (validationResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(Map.of("message", "Unauthorized access"), HttpStatus.UNAUTHORIZED);
        }

        Map<String, Object> prescriptionData = prescriptionService.getPrescription(appointmentId);
        if (prescriptionData == null || prescriptionData.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "No prescription found for this appointment"), HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(prescriptionData);
    }
}

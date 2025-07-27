package com.example.controller;

import com.example.model.Doctor;
import com.example.model.Login;
import com.example.service.DoctorService;
import com.example.service.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.path}doctor")
public class DoctorController {

    private final DoctorService doctorService;
    private final Service service;

    @Autowired
    public DoctorController(DoctorService doctorService, Service service) {
        this.doctorService = doctorService;
        this.service = service;
    }

    // ✅ 1. Get Doctor Availability
    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(
            @PathVariable String user,
            @PathVariable Long doctorId,
            @PathVariable String date,
            @PathVariable String token) {

        ResponseEntity<Map<String, String>> validationResponse = service.validateToken(token, user);
        if (validationResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(Map.of("message", "Unauthorized access"), HttpStatus.UNAUTHORIZED);
        }

        LocalDate localDate = LocalDate.parse(date);
        return ResponseEntity.ok(doctorService.getDoctorAvailability(doctorId, localDate));
    }

    // ✅ 2. Get List of Doctors
    @GetMapping
    public ResponseEntity<List<Doctor>> getDoctors() {
        return ResponseEntity.ok(doctorService.getDoctors());
    }

    // ✅ 3. Add New Doctor
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> addDoctor(
            @PathVariable String token,
            @RequestBody Doctor doctor) {

        ResponseEntity<Map<String, String>> validationResponse = service.validateToken(token, "admin");
        if (validationResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(Map.of("message", "Unauthorized access"), HttpStatus.UNAUTHORIZED);
        }

        int result = doctorService.saveDoctor(doctor);
        return switch (result) {
            case 1 -> new ResponseEntity<>(Map.of("message", "Doctor added to db"), HttpStatus.CREATED);
            case -1 -> new ResponseEntity<>(Map.of("message", "Doctor already exists"), HttpStatus.CONFLICT);
            default -> new ResponseEntity<>(Map.of("message", "Some internal error occurred"), HttpStatus.INTERNAL_SERVER_ERROR);
        };
    }

    // ✅ 4. Doctor Login
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> doctorLogin(@RequestBody Login login) {
        return doctorService.validateDoctor(login);
    }

    // ✅ 5. Update Doctor Details
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateDoctor(
            @PathVariable String token,
            @RequestBody Doctor doctor) {

        ResponseEntity<Map<String, String>> validationResponse = service.validateToken(token, "admin");
        if (validationResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(Map.of("message", "Unauthorized access"), HttpStatus.UNAUTHORIZED);
        }

        int result = doctorService.updateDoctor(doctor);
        return switch (result) {
            case 1 -> ResponseEntity.ok(Map.of("message", "Doctor updated"));
            case -1 -> new ResponseEntity<>(Map.of("message", "Doctor not found"), HttpStatus.NOT_FOUND);
            default -> new ResponseEntity<>(Map.of("message", "Some internal error occurred"), HttpStatus.INTERNAL_SERVER_ERROR);
        };
    }

    // ✅ 6. Delete Doctor
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> deleteDoctor(
            @PathVariable Long id,
            @PathVariable String token) {

        ResponseEntity<Map<String, String>> validationResponse = service.validateToken(token, "admin");
        if (validationResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(Map.of("message", "Unauthorized access"), HttpStatus.UNAUTHORIZED);
        }

        int result = doctorService.deleteDoctor(id);
        return switch (result) {
            case 1 -> ResponseEntity.ok(Map.of("message", "Doctor deleted successfully"));
            case -1 -> new ResponseEntity<>(Map.of("message", "Doctor not found with id"), HttpStatus.NOT_FOUND);
            default -> new ResponseEntity<>(Map.of("message", "Some internal error occurred"), HttpStatus.INTERNAL_SERVER_ERROR);
        };
    }

    // ✅ 7. Filter Doctors
    @GetMapping("/filter/{name}/{time}/{speciality}")
    public ResponseEntity<Map<String, Object>> filterDoctors(
            @PathVariable String name,
            @PathVariable String time,
            @PathVariable String speciality) {

        return ResponseEntity.ok(service.filterDoctor(name, time, speciality));
    }
}

package com.example.repository;

import com.example.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // ✅ Find patient by email
    Patient findByEmail(String email);

    // ✅ Find patient by email OR phone (for multi-field validation)
    Patient findByEmailOrPhone(String email, String phone);
}

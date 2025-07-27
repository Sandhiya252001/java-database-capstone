package com.example.repository;

import com.example.model.Prescription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends MongoRepository<Prescription, String> {

    // ✅ Custom method to find prescriptions by appointment ID
    List<Prescription> findByAppointmentId(Long appointmentId);
}

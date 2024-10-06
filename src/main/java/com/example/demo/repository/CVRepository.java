package com.example.demo.repository;

import com.example.demo.entity.CV;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CVRepository extends JpaRepository<CV, Long> {
    CV findByFirstNameAndLastName(String firstName, String lastName);
    Optional<CV> findById(Long id);
}

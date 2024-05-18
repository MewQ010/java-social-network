package com.example.demo.repository;

import com.example.demo.entity.ReportMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportMessageRepository extends JpaRepository<ReportMessage, Long> {
    Optional<ReportMessage> findById(Long id);
    Optional<ReportMessage> findByUserId(Long id);
}

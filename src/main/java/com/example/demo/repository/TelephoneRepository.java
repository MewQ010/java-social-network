package com.example.demo.repository;

import com.example.demo.entity.Telephone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelephoneRepository extends JpaRepository<Telephone, Long> {

}

package com.example.demo.repository;

import com.example.demo.entity.PersonalData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.StringTokenizer;

public interface UserDataRepository extends JpaRepository<PersonalData, Long> {
    PersonalData findByEmail(String email);
    PersonalData findByFirstNameAndLastName(String firstname, String lastname);

    boolean existsByEmail(String email);
}

package com.example.demo.repository;

import com.example.demo.entity.PersonalData;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
  List<User> findByLogin(String login);
  User findByPersonalData(PersonalData personalData);
  boolean existsByLogin(String login);
  List<User> findByLoginStartingWithIgnoreCase(String login);

}

package com.example.demo.repository;

import com.example.demo.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findBySenderAndReceiverOrSenderAndReceiver(String sender, String receiver, String receiver2, String sender2);
}
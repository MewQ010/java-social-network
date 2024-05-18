package com.example.demo.repository;

import com.example.demo.entity.Message;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySenderAndReceiverOrSenderAndReceiverOrderByTimestampAsc(String sender, String receiver, String receiver2, String sender2);
    List<Message> findBySenderOrReceiver(String sender, String receiver);
    List<Message> findByChannel(String channel);


}
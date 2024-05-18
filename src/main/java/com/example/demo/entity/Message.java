package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Data
@Entity
@Table(name = "messages")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "sender")
    private String sender;
    @Column(name = "channel")
    private String channel;
    @Column(name = "receiver")
    private String receiver;
    @Column(name = "content")
    private String content;
    @Column(name = "timestamp")
    private Date timestamp;
}
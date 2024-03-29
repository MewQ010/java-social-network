package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name = "chat")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id")
    private Long id;
    @Column(name = "channel")
    private String channel;
    @Column(name = "sender")
    private String sender;
    @Column(name = "receiver")
    private String receiver;
}

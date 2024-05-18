package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "report_messages")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "entity_id")
    private Long entityId;
    @Column(name = "entity")
    private String entity;
    @Column(name = "content")
    private String content;

}

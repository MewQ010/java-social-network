package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "cv")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CV {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cv_id")
    private Long id;
    @Column(name = "firstName")
    private String firstName;
    @Column(name = "lastName")
    private String lastName;
    @Column(name = "description")
    private String description;
    @Column(name = "image_url")
    private String image_url;
    @Column(name = "github")
    private String github_link;
    @Column(name = "telegram")
    private String telegram_link;
    @Column(name = "instagram")
    private String instagram_link;
}

package com.example.demo.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;


@Data
@Entity
@Table(name = "posts")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;
    @Column(name = "post_image")
    private String postImage;
    @Column(name = "header")
    private String header;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "description")
    private String description;
    @Column(name = "time")
    private String time;
    @Column(name = "image")
    private String image;

    @ElementCollection
    @CollectionTable(name = "post_likes", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "user_id")
    private List<Long> likeList = new ArrayList<>();
}

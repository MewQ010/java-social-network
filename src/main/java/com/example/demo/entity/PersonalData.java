package com.example.demo.entity;

import java.time.LocalDate;
import java.util.Set;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Entity
@Table(name = "personal_data")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "personal_data_id")
    private Long id;
    @OneToOne
    @JoinColumn(name = "telephone_id")
    private Telephone telephone;
    @OneToMany(mappedBy = "id", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Post> posts;
    @ManyToMany
    @JoinTable(
            name = "personal_data_paragraphs_relations",
            joinColumns = @JoinColumn(name = "personal_data_id"),
            inverseJoinColumns = @JoinColumn(name = "paragraph_id"))
    private Set<Paragraph> paragraphs;

    @ManyToMany
    @JoinTable(
            name = "personal_data_tool_relations",
            joinColumns = @JoinColumn(name = "peesona_data_id"),
            inverseJoinColumns = @JoinColumn(name = "tool_id"))
    private Set<Tool> tools;

    @ManyToMany
    @JoinTable(
            name = "personal_data_skill_relations",
            joinColumns = @JoinColumn(name = "personal_data_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private Set<Skill> skills;

    @ManyToMany
    @JoinTable(
            name = "personal_data_task_relations",
            joinColumns = @JoinColumn(name = "personal_data_id"),
            inverseJoinColumns = @JoinColumn(name = "task_id"))
    private Set<Task> tasks;

    @ManyToMany
    @JoinTable(
            name = "personal_data_experience_relations",
            joinColumns = @JoinColumn(name = "personal_data_id"),
            inverseJoinColumns = @JoinColumn(name = "experience_id"))
    private Set<Experience> experiences;

    @Column(name = "email")
    private String email;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    @Column(name = "profile_image_url")
    private String profileImageUrl;
    @Column(name = "description")
    private String description;
}

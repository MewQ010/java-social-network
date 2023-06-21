package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

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
    @OneToOne
    @JoinColumn(name = "post_id")
    private Post post;
    @ManyToMany
    @JoinTable(
            name = "personal_data_paragraphs_relations",
            joinColumns = @JoinColumn(name = "personal_data_id"),
            inverseJoinColumns = @JoinColumn(name = "paragraph_id"))
    private Set<Paragraph> paragraphs;
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
}

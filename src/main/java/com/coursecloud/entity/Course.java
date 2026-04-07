package com.coursecloud.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String category;

    private String level;  // Beginner, Intermediate, Advanced

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.draft;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User instructor;

    @Builder.Default
    private int students = 0;

    @Builder.Default
    private double rating = 0.0;

    private String thumbnailGradient;
    private String thumbnailIcon;
    
    private String duration;
    private int lessons;
    
    @Column(columnDefinition = "TEXT")
    private String tags; // Stored as comma-separated
    
    @Column(columnDefinition = "TEXT")
    private String syllabus; // Stored as JSON or newline separated
    
    @Column(columnDefinition = "TEXT")
    private String instructorBio;

    @Builder.Default
    private LocalDate createdAt = LocalDate.now();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Enrollment> enrollments = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Assignment> assignments = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ContentItem> contentItems = new ArrayList<>();

    public enum Status {
        draft, published, archived
    }
}

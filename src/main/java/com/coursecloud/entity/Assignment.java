package com.coursecloud.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "assignments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDate dueDate;

    @Builder.Default
    private int maxScore = 100;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.active;

    @Builder.Default
    private int submissionsCount = 0;

    @Builder.Default
    private int gradedCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User instructor;

    @Builder.Default
    private LocalDate createdAt = LocalDate.now();

    public enum Status {
        active, closed
    }
}

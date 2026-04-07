package com.coursecloud.dto;

import com.coursecloud.entity.Assignment;
import lombok.Data;
import java.time.LocalDate;

@Data
public class AssignmentDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private int maxScore;
    private String status;
    private int submissionsCount;
    private int gradedCount;
    private Long courseId;
    private String courseName;
    private Long instructorId;
    private String instructorName;
    private LocalDate createdAt;

    public static AssignmentDTO fromEntity(Assignment a) {
        AssignmentDTO dto = new AssignmentDTO();
        dto.setId(a.getId());
        dto.setTitle(a.getTitle());
        dto.setDescription(a.getDescription());
        dto.setDueDate(a.getDueDate());
        dto.setMaxScore(a.getMaxScore());
        dto.setStatus(a.getStatus().name());
        dto.setSubmissionsCount(a.getSubmissionsCount());
        dto.setGradedCount(a.getGradedCount());
        dto.setCourseId(a.getCourse().getId());
        dto.setCourseName(a.getCourse().getTitle());
        dto.setInstructorId(a.getInstructor().getId());
        dto.setInstructorName(a.getInstructor().getName());
        dto.setCreatedAt(a.getCreatedAt());
        return dto;
    }
}

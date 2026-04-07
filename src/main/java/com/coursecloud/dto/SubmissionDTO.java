package com.coursecloud.dto;

import com.coursecloud.entity.Submission;
import lombok.Data;
import java.time.LocalDate;

@Data
public class SubmissionDTO {
    private Long id;
    private Long assignmentId;
    private String assignmentTitle;
    private String courseName;
    private Long courseId;
    private Long studentId;
    private String studentName;
    private String studentInitials;
    private String answer;
    private Integer score;
    private String feedback;
    private String status;
    private LocalDate submittedAt;

    public static SubmissionDTO fromEntity(Submission s) {
        SubmissionDTO dto = new SubmissionDTO();
        dto.setId(s.getId());
        dto.setAssignmentId(s.getAssignment().getId());
        dto.setAssignmentTitle(s.getAssignment().getTitle());
        dto.setCourseName(s.getAssignment().getCourse().getTitle());
        dto.setCourseId(s.getAssignment().getCourse().getId());
        dto.setStudentId(s.getStudent().getId());
        dto.setStudentName(s.getStudent().getName());
        dto.setStudentInitials(s.getStudent().getInitials());
        dto.setAnswer(s.getAnswer());
        dto.setScore(s.getScore());
        dto.setFeedback(s.getFeedback());
        dto.setStatus(s.getStatus().name());
        dto.setSubmittedAt(s.getSubmittedAt());
        return dto;
    }
}

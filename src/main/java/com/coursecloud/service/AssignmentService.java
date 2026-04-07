package com.coursecloud.service;

import com.coursecloud.dto.AssignmentDTO;
import com.coursecloud.dto.SubmissionDTO;
import com.coursecloud.entity.*;
import com.coursecloud.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AssignmentService {

    private static final Logger log = LoggerFactory.getLogger(AssignmentService.class);

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public AssignmentService(AssignmentRepository assignmentRepository,
                             SubmissionRepository submissionRepository,
                             CourseRepository courseRepository,
                             EnrollmentRepository enrollmentRepository) {
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    // ── ASSIGNMENTS ──────────────────────────────────────────────────

    public List<AssignmentDTO> getAllAssignments() {
        return assignmentRepository.findAll().stream()
                .map(AssignmentDTO::fromEntity).collect(Collectors.toList());
    }

    public List<AssignmentDTO> getAssignmentsByInstructor(Long instructorId) {
        return assignmentRepository.findByInstructorId(instructorId).stream()
                .map(AssignmentDTO::fromEntity).collect(Collectors.toList());
    }

    public List<AssignmentDTO> getAssignmentsByStudent(Long studentId) {
        List<Long> courseIds = enrollmentRepository.findByStudentId(studentId).stream()
                .map(e -> e.getCourse().getId()).collect(Collectors.toList());
        return assignmentRepository.findByCourseIdIn(courseIds).stream()
                .map(AssignmentDTO::fromEntity).collect(Collectors.toList());
    }

    public List<AssignmentDTO> getAssignmentsByCourse(Long courseId) {
        return assignmentRepository.findByCourseId(courseId).stream()
                .map(AssignmentDTO::fromEntity).collect(Collectors.toList());
    }

    public AssignmentDTO createAssignment(AssignmentDTO dto, User instructor) {
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + dto.getCourseId()));

        boolean isOwner = course.getInstructor().getId().equals(instructor.getId());
        if (!isOwner && instructor.getRole() != User.Role.Admin) {
            throw new SecurityException("You can only create assignments for your own courses.");
        }

        Assignment a = Assignment.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .dueDate(dto.getDueDate())
                .maxScore(dto.getMaxScore() > 0 ? dto.getMaxScore() : 100)
                .course(course)
                .instructor(instructor)
                .build();

        Assignment saved = assignmentRepository.save(a);
        log.info("Assignment created: '{}' by {}", saved.getTitle(), instructor.getEmail());
        return AssignmentDTO.fromEntity(saved);
    }

    public void deleteAssignment(Long id, User currentUser) {
        Assignment a = assignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + id));
        boolean isOwner = a.getInstructor().getId().equals(currentUser.getId());
        if (!isOwner && currentUser.getRole() != User.Role.Admin) {
            throw new SecurityException("Not authorized to delete this assignment.");
        }
        assignmentRepository.deleteById(id);
        log.info("Assignment deleted: {}", id);
    }

    // ── SUBMISSIONS ──────────────────────────────────────────────────

    public SubmissionDTO submitAssignment(Long assignmentId, String answer, User student) {
        if (submissionRepository.existsByAssignmentIdAndStudentId(assignmentId, student.getId())) {
            throw new IllegalArgumentException("You have already submitted this assignment.");
        }

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + assignmentId));

        Submission sub = Submission.builder()
                .assignment(assignment)
                .student(student)
                .answer(answer)
                .status(Submission.Status.pending)
                .build();

        Submission saved = submissionRepository.save(sub);
        assignment.setSubmissionsCount(assignment.getSubmissionsCount() + 1);
        assignmentRepository.save(assignment);
        log.info("Submission created for assignment {} by student {}", assignmentId, student.getEmail());
        return SubmissionDTO.fromEntity(saved);
    }

    public SubmissionDTO gradeSubmission(Long submissionId, Integer score, String feedback, User instructor) {
        Submission sub = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found: " + submissionId));

        boolean wasGraded = sub.getStatus() == Submission.Status.graded;
        sub.setScore(score);
        sub.setFeedback(feedback);
        sub.setStatus(Submission.Status.graded);
        submissionRepository.save(sub);

        if (!wasGraded) {
            Assignment a = sub.getAssignment();
            a.setGradedCount(a.getGradedCount() + 1);
            assignmentRepository.save(a);
        }

        log.info("Submission {} graded with score {} by {}", submissionId, score, instructor.getEmail());
        return SubmissionDTO.fromEntity(sub);
    }

    public List<SubmissionDTO> getSubmissionsByInstructor(Long instructorId) {
        List<Long> assignmentIds = assignmentRepository.findByInstructorId(instructorId).stream()
                .map(Assignment::getId).collect(Collectors.toList());
        return submissionRepository.findByAssignmentIdIn(assignmentIds).stream()
                .map(SubmissionDTO::fromEntity).collect(Collectors.toList());
    }

    public List<SubmissionDTO> getSubmissionsByStudent(Long studentId) {
        return submissionRepository.findByStudentId(studentId).stream()
                .map(SubmissionDTO::fromEntity).collect(Collectors.toList());
    }
}

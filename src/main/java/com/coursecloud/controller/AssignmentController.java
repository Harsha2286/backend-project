package com.coursecloud.controller;

import com.coursecloud.dto.AssignmentDTO;
import com.coursecloud.dto.SubmissionDTO;
import com.coursecloud.entity.User;
import com.coursecloud.service.AssignmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assignments")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"})
public class AssignmentController {

    private static final Logger log = LoggerFactory.getLogger(AssignmentController.class);

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    /**
     * GET /api/assignments
     * Returns assignments scoped to the current user's role.
     */
    @GetMapping
    public ResponseEntity<List<AssignmentDTO>> getAssignments(
            @AuthenticationPrincipal User currentUser) {
        return switch (currentUser.getRole()) {
            case Admin -> ResponseEntity.ok(assignmentService.getAllAssignments());
            case Instructor -> ResponseEntity.ok(
                assignmentService.getAssignmentsByInstructor(currentUser.getId()));
            case Student -> ResponseEntity.ok(
                assignmentService.getAssignmentsByStudent(currentUser.getId()));
            default -> ResponseEntity.ok(List.of());
        };
    }

    /**
     * GET /api/assignments/course/{courseId}
     * Returns all assignments for a specific course.
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<AssignmentDTO>> getByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByCourse(courseId));
    }

    /**
     * POST /api/assignments
     * Creates a new assignment (Instructor or Admin only).
     */
    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody AssignmentDTO dto,
            @AuthenticationPrincipal User currentUser) {
        try {
            AssignmentDTO created = assignmentService.createAssignment(dto, currentUser);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException | SecurityException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/assignments/{id}
     * Deletes an assignment (owner Instructor or Admin).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        try {
            assignmentService.deleteAssignment(id, currentUser);
            return ResponseEntity.ok(Map.of("message", "Assignment deleted."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    // ── SUBMISSIONS ──────────────────────────────────────────────────

    /**
     * POST /api/assignments/{id}/submit
     * Submits an answer to an assignment (Student only).
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submit(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal User currentUser) {
        try {
            String answer = body.getOrDefault("answer", "");
            SubmissionDTO submission = assignmentService.submitAssignment(id, answer, currentUser);
            return ResponseEntity.ok(submission);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/assignments/submissions
     * Returns submissions scoped to the current user.
     */
    @GetMapping("/submissions")
    public ResponseEntity<List<SubmissionDTO>> getSubmissions(
            @AuthenticationPrincipal User currentUser) {
        return switch (currentUser.getRole()) {
            case Admin, Instructor -> ResponseEntity.ok(
                assignmentService.getSubmissionsByInstructor(currentUser.getId()));
            case Student -> ResponseEntity.ok(
                assignmentService.getSubmissionsByStudent(currentUser.getId()));
            default -> ResponseEntity.ok(List.of());
        };
    }

    /**
     * PUT /api/assignments/submissions/{submissionId}/grade
     * Grades a submission (Instructor or Admin only).
     */
    @PutMapping("/submissions/{submissionId}/grade")
    public ResponseEntity<?> grade(
            @PathVariable Long submissionId,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal User currentUser) {
        try {
            Integer score = (Integer) body.get("score");
            String feedback = (String) body.getOrDefault("feedback", "");
            SubmissionDTO result = assignmentService.gradeSubmission(submissionId, score, feedback, currentUser);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

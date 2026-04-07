package com.coursecloud.controller;

import com.coursecloud.dto.CourseDTO;
import com.coursecloud.entity.User;
import com.coursecloud.service.CourseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"})
public class CourseController {

    private static final Logger log = LoggerFactory.getLogger(CourseController.class);

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * GET /api/courses
     * Returns all courses (Admin) or published courses for others.
     */
    @GetMapping
    public ResponseEntity<List<CourseDTO>> getCourses(
            @AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() == User.Role.Admin) {
            return ResponseEntity.ok(courseService.getAllCourses());
        } else if (currentUser.getRole() == User.Role.Instructor) {
            return ResponseEntity.ok(courseService.getCoursesByInstructor(currentUser.getId()));
        } else {
            return ResponseEntity.ok(courseService.getPublishedCourses());
        }
    }

    /**
     * GET /api/courses/published
     * Returns all published courses.
     */
    @GetMapping("/published")
    public ResponseEntity<List<CourseDTO>> getPublishedCourses() {
        return ResponseEntity.ok(courseService.getPublishedCourses());
    }

    /**
     * GET /api/courses/my-learning
     * Returns courses the authenticated student is enrolled in.
     */
    @GetMapping("/my-learning")
    public ResponseEntity<List<CourseDTO>> getMyLearning(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(courseService.getEnrolledCourses(currentUser.getId()));
    }

    /**
     * GET /api/courses/instructor/{instructorId}
     * Returns courses created by a specific instructor.
     */
    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<List<CourseDTO>> getCoursesByInstructor(
            @PathVariable Long instructorId) {
        return ResponseEntity.ok(courseService.getCoursesByInstructor(instructorId));
    }

    /**
     * GET /api/courses/{id}
     * Returns one course by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCourseById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(courseService.getCourseById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/courses
     * Creates a new course (Instructor or Admin only).
     */
    @PostMapping
    public ResponseEntity<?> createCourse(
            @RequestBody CourseDTO courseDTO,
            @AuthenticationPrincipal User currentUser) {
        try {
            CourseDTO created = courseService.createCourse(courseDTO, currentUser);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/courses/{id}
     * Updates an existing course.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCourse(
            @PathVariable Long id,
            @RequestBody CourseDTO courseDTO,
            @AuthenticationPrincipal User currentUser) {
        try {
            CourseDTO updated = courseService.updateCourse(id, courseDTO, currentUser);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/courses/{id}
     * Deletes a course (Admin only — enforced by SecurityConfig).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        try {
            courseService.deleteCourse(id);
            return ResponseEntity.ok(Map.of("message", "Course deleted successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/courses/{id}/enroll
     * Enrolls the authenticated student in a course.
     */
    @PostMapping("/{id}/enroll")
    public ResponseEntity<?> enroll(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        try {
            String message = courseService.enrollStudent(id, currentUser);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/courses/{id}/is-enrolled
     * Checks if the current user is enrolled in a course.
     */
    @GetMapping("/{id}/is-enrolled")
    public ResponseEntity<?> isEnrolled(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        boolean enrolled = courseService.isEnrolled(id, currentUser.getId());
        return ResponseEntity.ok(Map.of("enrolled", enrolled));
    }
}

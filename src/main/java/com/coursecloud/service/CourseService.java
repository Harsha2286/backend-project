package com.coursecloud.service;

import com.coursecloud.dto.CourseDTO;
import com.coursecloud.entity.Course;
import com.coursecloud.entity.Enrollment;
import com.coursecloud.entity.User;
import com.coursecloud.repository.CourseRepository;
import com.coursecloud.repository.EnrollmentRepository;
import com.coursecloud.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CourseService {

    private static final Logger log = LoggerFactory.getLogger(CourseService.class);

    private static final String[][] THUMBNAILS = {
        {"linear-gradient(135deg, #667eea, #764ba2)", "💻"},
        {"linear-gradient(135deg, #f093fb, #f5576c)", "🎨"},
        {"linear-gradient(135deg, #4facfe, #00f2fe)", "📊"},
        {"linear-gradient(135deg, #43e97b, #38f9d7)", "🔬"},
        {"linear-gradient(135deg, #fa709a, #fee140)", "📐"},
        {"linear-gradient(135deg, #a18cd1, #fbc2eb)", "🤖"},
        {"linear-gradient(135deg, #ffecd2, #fcb69f)", "📸"},
        {"linear-gradient(135deg, #a1c4fd, #c2e9fb)", "📱"},
    };

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    public CourseService(CourseRepository courseRepository,
                         EnrollmentRepository enrollmentRepository,
                         UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
    }

    // ── GET ALL (admin) ──────────────────────────────────────────────

    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(CourseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ── GET PUBLISHED ────────────────────────────────────────────────

    public List<CourseDTO> getPublishedCourses() {
        return courseRepository.findByStatus(Course.Status.published).stream()
                .map(CourseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ── GET BY INSTRUCTOR ────────────────────────────────────────────

    public List<CourseDTO> getCoursesByInstructor(Long instructorId) {
        return courseRepository.findByInstructorId(instructorId).stream()
                .map(CourseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ── GET ENROLLED (by student) ────────────────────────────────────

    public List<CourseDTO> getEnrolledCourses(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(Enrollment::getCourse)
                .map(CourseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ── GET ONE ──────────────────────────────────────────────────────

    public CourseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + id));
        return CourseDTO.fromEntity(course);
    }

    // ── CREATE ───────────────────────────────────────────────────────

    public CourseDTO createCourse(CourseDTO courseDTO, User instructor) {
        long count = courseRepository.count();
        int idx = (int) (count % THUMBNAILS.length);

        Course.Status courseStatus = Course.Status.draft;
        try {
            if (courseDTO.getStatus() != null) {
                courseStatus = Course.Status.valueOf(courseDTO.getStatus());
            }
        } catch (IllegalArgumentException e) {
            // keep draft
        }

        Course course = Course.builder()
                .title(courseDTO.getTitle())
                .description(courseDTO.getDescription())
                .category(courseDTO.getCategory())
                .level(courseDTO.getLevel())
                .status(courseStatus)
                .instructor(instructor)
                .duration(courseDTO.getDuration())
                .lessons(courseDTO.getLessons())
                .tags(courseDTO.getTags() != null ? String.join(",", courseDTO.getTags()) : "")
                .syllabus(courseDTO.getSyllabus() != null ? String.join("\n", courseDTO.getSyllabus()) : "")
                .instructorBio(courseDTO.getInstructorBio())
                .thumbnailGradient(THUMBNAILS[idx][0])
                .thumbnailIcon(THUMBNAILS[idx][1])
                .build();

        Course saved = courseRepository.save(course);
        log.info("Course created: '{}' by {}", saved.getTitle(), instructor.getEmail());
        return CourseDTO.fromEntity(saved);
    }

    // ── UPDATE ───────────────────────────────────────────────────────

    public CourseDTO updateCourse(Long id, CourseDTO courseDTO, User currentUser) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + id));

        boolean isAdmin = currentUser.getRole() == User.Role.Admin;
        boolean isOwner = course.getInstructor().getId().equals(currentUser.getId());
        if (!isAdmin && !isOwner) {
            throw new SecurityException("You are not authorized to update this course.");
        }

        if (courseDTO.getTitle() != null)       course.setTitle(courseDTO.getTitle());
        if (courseDTO.getDescription() != null) course.setDescription(courseDTO.getDescription());
        if (courseDTO.getCategory() != null)    course.setCategory(courseDTO.getCategory());
        if (courseDTO.getLevel() != null)       course.setLevel(courseDTO.getLevel());
        if (courseDTO.getStatus() != null) {
            try {
                course.setStatus(Course.Status.valueOf(courseDTO.getStatus()));
            } catch (Exception e) {}
        }
        if (courseDTO.getDuration() != null)    course.setDuration(courseDTO.getDuration());
        if (courseDTO.getLessons() > 0)         course.setLessons(courseDTO.getLessons());
        if (courseDTO.getTags() != null)        course.setTags(String.join(",", courseDTO.getTags()));
        if (courseDTO.getSyllabus() != null)    course.setSyllabus(String.join("\n", courseDTO.getSyllabus()));
        if (courseDTO.getInstructorBio() != null) course.setInstructorBio(courseDTO.getInstructorBio());

        log.info("Course updated: {} by {}", id, currentUser.getEmail());
        return CourseDTO.fromEntity(courseRepository.save(course));
    }

    // ── DELETE ───────────────────────────────────────────────────────

    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new IllegalArgumentException("Course not found: " + id);
        }
        courseRepository.deleteById(id);
        log.info("Course deleted: {}", id);
    }

    // ── ENROLL ───────────────────────────────────────────────────────

    public String enrollStudent(Long courseId, User student) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            return "Already enrolled in this course.";
        }

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .build();
        enrollmentRepository.save(enrollment);
        course.setStudents(course.getStudents() + 1);
        courseRepository.save(course);

        log.info("Student {} enrolled in course {}", student.getEmail(), courseId);
        return "Successfully enrolled in " + course.getTitle();
    }

    // ── CHECK ENROLLMENT ─────────────────────────────────────────────

    public boolean isEnrolled(Long courseId, Long studentId) {
        return enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId);
    }
}

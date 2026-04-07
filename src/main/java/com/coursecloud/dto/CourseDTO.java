package com.coursecloud.dto;

import com.coursecloud.entity.Course;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

@Data
public class CourseDTO {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String level;
    private String status;
    private Long createdBy;       // frontend uses createdBy
    private String createdByName; // frontend uses createdByName
    private int students;         // frontend uses students
    private double rating;
    private String thumbnailGradient;
    private String thumbnailIcon;
    private String duration;
    private int lessons;
    private List<String> tags;
    private List<String> syllabus;
    private String instructorBio;
    private LocalDate createdAt;

    public static CourseDTO fromEntity(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setCategory(course.getCategory());
        dto.setLevel(course.getLevel());
        dto.setStatus(course.getStatus().name());
        dto.setCreatedBy(course.getInstructor().getId());
        dto.setCreatedByName(course.getInstructor().getName());
        dto.setStudents(course.getStudents());
        dto.setRating(course.getRating());
        dto.setThumbnailGradient(course.getThumbnailGradient());
        dto.setThumbnailIcon(course.getThumbnailIcon());
        dto.setDuration(course.getDuration());
        dto.setLessons(course.getLessons());
        
        dto.setTags(course.getTags() != null && !course.getTags().isBlank() 
                ? Arrays.asList(course.getTags().split(",")) 
                : new ArrayList<>());
                
        dto.setSyllabus(course.getSyllabus() != null && !course.getSyllabus().isBlank() 
                ? Arrays.asList(course.getSyllabus().split("\n")) 
                : new ArrayList<>());
                
        dto.setInstructorBio(course.getInstructorBio());
        dto.setCreatedAt(course.getCreatedAt());
        return dto;
    }
}

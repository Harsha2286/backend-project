package com.coursecloud.repository;

import com.coursecloud.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByCourseId(Long courseId);

    List<Assignment> findByInstructorId(Long instructorId);

    List<Assignment> findByCourseIdIn(List<Long> courseIds);
}

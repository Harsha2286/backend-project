package com.coursecloud.repository;

import com.coursecloud.entity.ContentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentItemRepository extends JpaRepository<ContentItem, Long> {

    List<ContentItem> findByCourseId(Long courseId);

    List<ContentItem> findByCreatorId(Long creatorId);
}

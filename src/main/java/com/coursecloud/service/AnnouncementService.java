package com.coursecloud.service;

import com.coursecloud.entity.Announcement;
import com.coursecloud.entity.User;
import com.coursecloud.repository.AnnouncementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AnnouncementService {

    private static final Logger log = LoggerFactory.getLogger(AnnouncementService.class);

    private final AnnouncementRepository announcementRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    public List<Announcement> getAll() {
        return announcementRepository.findAllByOrderByCreatedAtDesc();
    }

    public Announcement create(String title, String content,
                                Announcement.Priority priority, User author) {
        Announcement ann = Announcement.builder()
                .title(title)
                .content(content)
                .priority(priority)
                .author(author)
                .build();
        Announcement saved = announcementRepository.save(ann);
        log.info("Announcement created by {}: '{}'", author.getEmail(), title);
        return saved;
    }

    public void delete(Long id) {
        announcementRepository.deleteById(id);
        log.info("Announcement deleted: {}", id);
    }
}

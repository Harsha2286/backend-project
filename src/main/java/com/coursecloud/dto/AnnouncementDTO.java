package com.coursecloud.dto;

import com.coursecloud.entity.Announcement;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AnnouncementDTO {
    private Long id;
    private String title;
    private String content;
    private String priority;
    private Long authorId;
    private String authorName;
    private String authorRole;
    private LocalDate createdAt;

    public static AnnouncementDTO fromEntity(Announcement a) {
        AnnouncementDTO dto = new AnnouncementDTO();
        dto.setId(a.getId());
        dto.setTitle(a.getTitle());
        dto.setContent(a.getContent());
        dto.setPriority(a.getPriority().name());
        if (a.getAuthor() != null) {
            dto.setAuthorId(a.getAuthor().getId());
            dto.setAuthorName(a.getAuthor().getName());
            dto.setAuthorRole(a.getAuthor().getRole().name());
        }
        dto.setCreatedAt(a.getCreatedAt());
        return dto;
    }
}

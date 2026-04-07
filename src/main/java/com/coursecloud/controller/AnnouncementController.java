package com.coursecloud.controller;

import com.coursecloud.entity.Announcement;
import com.coursecloud.entity.User;
import com.coursecloud.service.AnnouncementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/announcements")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"})
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    /**
     * GET /api/announcements
     * Returns all announcements (all authenticated users can read).
     */
    @GetMapping
    public ResponseEntity<List<Announcement>> getAll() {
        return ResponseEntity.ok(announcementService.getAll());
    }

    /**
     * POST /api/announcements
     * Creates a new announcement (Admin or Instructor).
     */
    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal User currentUser) {
        String title    = body.get("title");
        String content  = body.get("content");
        String priority = body.getOrDefault("priority", "medium");

        if (title == null || content == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Title and content are required."));
        }

        Announcement.Priority p;
        try {
            p = Announcement.Priority.valueOf(priority);
        } catch (IllegalArgumentException e) {
            p = Announcement.Priority.medium;
        }

        Announcement saved = announcementService.create(title, content, p, currentUser);
        return ResponseEntity.ok(saved);
    }

    /**
     * DELETE /api/announcements/{id}
     * Deletes an announcement (Admin only).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Announcement deleted."));
    }
}

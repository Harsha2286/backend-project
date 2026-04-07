package com.coursecloud.dto;

import com.coursecloud.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String initials;
    private String status;
    private boolean emailVerified;
    private LocalDateTime joinedAt;
    private int courses;

    public static UserDTO fromEntity(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        dto.setInitials(user.getInitials());
        dto.setStatus(user.getStatus().name());
        dto.setEmailVerified(user.isEmailVerified());
        dto.setJoinedAt(user.getJoinedAt());
        dto.setCourses(user.getCourses());
        return dto;
    }
}

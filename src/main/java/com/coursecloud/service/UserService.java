package com.coursecloud.service;

import com.coursecloud.dto.UserDTO;
import com.coursecloud.entity.User;
import com.coursecloud.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ── GET ALL ──────────────────────────────────────────────────────

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ── GET ONE ──────────────────────────────────────────────────────

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        return UserDTO.fromEntity(user);
    }

    // ── UPDATE ───────────────────────────────────────────────────────

    public UserDTO updateUser(Long id, UserDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        if (dto.getName() != null)   user.setName(dto.getName());
        if (dto.getStatus() != null) user.setStatus(User.Status.valueOf(dto.getStatus()));
        if (dto.getRole() != null)   user.setRole(User.Role.valueOf(dto.getRole()));

        log.info("User updated: {}", id);
        return UserDTO.fromEntity(userRepository.save(user));
    }

    // ── DELETE ───────────────────────────────────────────────────────

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found: " + id);
        }
        userRepository.deleteById(id);
        log.info("User deleted: {}", id);
    }

    // ── TOGGLE STATUS ────────────────────────────────────────────────

    public UserDTO toggleStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        user.setStatus(user.getStatus() == User.Status.ACTIVE
            ? User.Status.INACTIVE : User.Status.ACTIVE);
        log.info("User {} status toggled to {}", id, user.getStatus());
        return UserDTO.fromEntity(userRepository.save(user));
    }
}

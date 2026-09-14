package com.ams.resident.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "email_verification_tokens")
@Getter
@Setter
public class EmailVerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String newEmail;

    @Column(nullable = false, unique = true)
    private String token = UUID.randomUUID().toString(); // Basic approach

    @Column(nullable = false)
    private LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);
}

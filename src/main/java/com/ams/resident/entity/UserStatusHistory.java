package com.ams.resident.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_status_history")
@Getter
@Setter
public class UserStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String userId;
    
    @Enumerated(EnumType.STRING)
    private UserStatus previousStatus;
    
    @Enumerated(EnumType.STRING)
    private UserStatus newStatus;
    
    private String reason;
    
    private String changedBy;
    
    private LocalDateTime changedAt = LocalDateTime.now();
}

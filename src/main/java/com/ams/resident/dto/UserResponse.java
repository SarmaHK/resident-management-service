package com.ams.resident.dto;

import com.ams.resident.entity.UserStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private UserStatus status;
    private List<String> roles;
    private List<StatusHistorySummary> statusHistory;
    
    @Getter
    @Setter
    public static class StatusHistorySummary {
        private UserStatus newStatus;
        private String reason;
        private String changedAt;
    }
}

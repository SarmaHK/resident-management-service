package com.ams.resident.dto;

import com.ams.resident.entity.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusRequest {
    @NotNull
    private UserStatus status;
    private String reason;
}

package com.ams.resident.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailConfirmRequest {
    @NotBlank
    private String verificationToken;
}

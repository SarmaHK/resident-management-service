package com.ams.resident.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailChangeRequest {
    
    @NotBlank(message = "New email is required")
    @Email(message = "New email must be a valid email format")
    private String newEmail;
}

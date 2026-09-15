package com.ams.resident.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileRequest {
    @NotBlank
    private String firstName;
    
    @NotBlank
    private String lastName;
    
    private String fullName;
    
    private String phone;
}

package com.ams.resident.dto;

import com.ams.resident.entity.ProfileType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileResponse {
    private String userId;
    private ProfileType profileType;
    private String firstName;
    private String lastName;
    private String phone;
    private String contactInfo;
    private String statusInfo;
}

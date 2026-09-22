package com.ams.resident.service;

import com.ams.resident.dto.ProfileRequest;
import com.ams.resident.dto.ProfileResponse;
import com.ams.resident.entity.Profile;
import com.ams.resident.exception.ResourceNotFoundException;
import com.ams.resident.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final AuditService auditService;

    public ProfileResponse getOwnProfile() {
        String userId = getAuthenticatedUserId();
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        return mapToResponse(profile);
    }

    @Transactional
    public ProfileResponse editOwnProfile(ProfileRequest request) {
        String userId = getAuthenticatedUserId();
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setPhone(request.getPhone());
        // Handle fullName mapping if applicable

        profile = profileRepository.save(profile);
        auditService.logEvent("FR-AUD-006", userId, "Updated own profile");

        return mapToResponse(profile);
    }

    private String getAuthenticatedUserId() {
        return com.ams.resident.security.SecurityUtils.getCurrentUserId();
    }
    
    private ProfileResponse mapToResponse(Profile profile) {
        ProfileResponse response = new ProfileResponse();
        response.setUserId(profile.getUserId());
        response.setProfileType(profile.getProfileType());
        response.setFirstName(profile.getFirstName());
        response.setLastName(profile.getLastName());
        response.setPhone(profile.getPhone());
        response.setStatusInfo("ACTIVE");
        return response;
    }
}

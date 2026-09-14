package com.ams.resident.service;

import com.ams.resident.dto.EmailChangeRequest;
import com.ams.resident.dto.EmailConfirmRequest;
import com.ams.resident.dto.ProfileRequest;
import com.ams.resident.dto.ProfileResponse;
import com.ams.resident.entity.EmailVerificationToken;
import com.ams.resident.entity.Profile;
import com.ams.resident.entity.User;
import com.ams.resident.exception.BadRequestException;
import com.ams.resident.exception.ResourceNotFoundException;
import com.ams.resident.repository.EmailVerificationTokenRepository;
import com.ams.resident.repository.ProfileRepository;
import com.ams.resident.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
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

    @Transactional
    public void requestEmailChange(EmailChangeRequest request) {
        String userId = getAuthenticatedUserId();
        
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUserId(userId);
        token.setNewEmail(request.getNewEmail());
        tokenRepository.save(token);
        
        // Emulate sending verification email with token
        auditService.logEvent("FR-AUD-006", userId, "Requested email change");
    }

    @Transactional
    public void confirmEmailChange(EmailConfirmRequest request) {
        String userId = getAuthenticatedUserId();
        
        EmailVerificationToken token = tokenRepository.findByToken(request.getVerificationToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired token"));
                
        if (!token.getUserId().equals(userId)) {
            throw new BadRequestException("Token does not belong to this user");
        }
        
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Token has expired");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                
        user.setEmail(token.getNewEmail());
        userRepository.save(user);
        tokenRepository.delete(token); // cleanup
        
        auditService.logEvent("FR-AUD-006", userId, "Confirmed email change to " + token.getNewEmail());
    }

    private String getAuthenticatedUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
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

package com.ams.resident.controller;

import com.ams.resident.dto.EmailChangeRequest;
import com.ams.resident.dto.ProfileRequest;
import com.ams.resident.dto.ProfileResponse;
import com.ams.resident.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profiles/me")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfileResponse> getOwnProfile() {
        return ResponseEntity.ok(profileService.getOwnProfile());
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> editOwnProfile(@Valid @RequestBody ProfileRequest request) {
        return ResponseEntity.ok(profileService.editOwnProfile(request));
    }

    @PostMapping("/email-change")
    public ResponseEntity<Void> requestEmailChange(@Valid @RequestBody EmailChangeRequest request) {
        profileService.requestEmailChange(request);
        return ResponseEntity.accepted().build();
    }
}

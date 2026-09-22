package com.ams.resident.controller;

import com.ams.resident.dto.ResidentRequest;
import com.ams.resident.dto.ResidentResponse;
import com.ams.resident.security.SecurityUtils;
import com.ams.resident.service.ResidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/residents")
@RequiredArgsConstructor
public class ResidentController {

    private final ResidentService residentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('APARTMENT_MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<ResidentResponse> createResident(@Valid @RequestBody ResidentRequest request) {
        ResidentResponse response = residentService.createResident(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('APARTMENT_MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<ResidentResponse>> getResidents() {
        return ResponseEntity.ok(residentService.getAllResidents());
    }

    @GetMapping("/{residentId}")
    public ResponseEntity<ResidentResponse> getResidentById(@PathVariable String residentId) {
        ResidentResponse resident = residentService.getResidentById(residentId);
        
        // Authorization: Admin or Self
        if (!isAdmin() && !resident.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new AccessDeniedException("You do not have permission to access this resident profile.");
        }
        
        return ResponseEntity.ok(resident);
    }

    @PutMapping("/{residentId}")
    public ResponseEntity<ResidentResponse> updateResident(@PathVariable String residentId, @Valid @RequestBody ResidentRequest request) {
        ResidentResponse resident = residentService.getResidentById(residentId);
        
        // Authorization: Admin or Self
        if (!isAdmin() && !resident.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new AccessDeniedException("You do not have permission to modify this resident profile.");
        }
        
        return ResponseEntity.ok(residentService.updateResident(residentId, request));
    }

    @PatchMapping("/{residentId}/status")
    @PreAuthorize("hasAnyRole('APARTMENT_MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<Void> updateResidentStatus(@PathVariable String residentId) {
        // BLOCKED: The approved schema (V1__init_schema.sql) strictly omits a status column for profiles.
        // Inventing a status field or transition rule is expressly prohibited by the requirements.
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;
        
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_APARTMENT_MANAGER") || 
                               a.getAuthority().equals("ROLE_SYSTEM_ADMIN"));
    }
}

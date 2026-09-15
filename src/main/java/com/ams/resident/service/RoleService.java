package com.ams.resident.service;

import com.ams.resident.entity.Role;
import com.ams.resident.entity.User;
import com.ams.resident.exception.BadRequestException;
import com.ams.resident.exception.ResourceNotFoundException;
import com.ams.resident.repository.RoleRepository;
import com.ams.resident.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuditService auditService;

    @Transactional
    public void assignRole(String userId, String roleName) {
        verifyAdminAccessAndNotSelf(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        // Add role if not present
        if (user.getRoles().stream().noneMatch(r -> r.getName().equals(roleName))) {
            user.getRoles().add(role);
            userRepository.save(user);
            auditService.logEvent("FR-AUD-003", getAuthenticatedUserId(), "Assigned role " + roleName + " to user " + userId);
        }
    }

    @Transactional
    public void removeRole(String userId, String roleName) {
        verifyAdminAccessAndNotSelf(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        if (user.getRoles().removeIf(r -> r.getName().equals(roleName))) {
            userRepository.save(user);
            auditService.logEvent("FR-AUD-003", getAuthenticatedUserId(), "Removed role " + roleName + " from user " + userId);
        }
    }

    private void verifyAdminAccessAndNotSelf(String targetUserId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = auth.getName(); // Since we use id as subject

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("SYSTEM_ADMINISTRATOR"));

        if (!isAdmin) {
            throw new AccessDeniedException("Only SYSTEM_ADMINISTRATOR can manage roles.");
        }

        if (currentUserId.equals(targetUserId)) {
            throw new AccessDeniedException("Cannot modify own roles.");
        }
    }

    private String getAuthenticatedUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}

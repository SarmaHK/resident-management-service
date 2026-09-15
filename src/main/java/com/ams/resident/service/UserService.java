package com.ams.resident.service;

import com.ams.resident.dto.PaginatedUserResponse;
import com.ams.resident.dto.UserRequest;
import com.ams.resident.dto.UserResponse;
import com.ams.resident.entity.Role;
import com.ams.resident.entity.User;
import com.ams.resident.entity.UserStatus;
import com.ams.resident.entity.UserStatusHistory;
import com.ams.resident.exception.ConflictException;
import com.ams.resident.exception.ResourceNotFoundException;
import com.ams.resident.repository.RoleRepository;
import com.ams.resident.repository.UserRepository;
import com.ams.resident.repository.UserStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserStatusHistoryRepository historyRepository;
    private final AuditService auditService;

    @Transactional
    @PreAuthorize("hasAuthority('SYSTEM_ADMINISTRATOR')")
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(UserStatus.ACTIVE);

        if (request.getInitialRoles() != null) {
            Set<Role> roles = request.getInitialRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        user = userRepository.save(user);

        // Stub for temporary password flow (usually triggers email here)
        // emailService.sendTemporaryPassword(user.getEmail(), generatedPassword);

        auditService.logEvent("FR-AUD-001", "system", "Created user " + user.getId());

        return mapToResponse(user);
    }

    @PreAuthorize("hasAuthority('SYSTEM_ADMINISTRATOR')")
    public PaginatedUserResponse listUsers(String query, UserStatus status, String role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.searchUsers(query, status, role, pageable);

        PaginatedUserResponse response = new PaginatedUserResponse();
        response.setData(userPage.getContent().stream().map(this::mapToResponse).collect(Collectors.toList()));
        
        PaginatedUserResponse.PageMetadata meta = new PaginatedUserResponse.PageMetadata();
        meta.setPage(page);
        meta.setSize(size);
        meta.setTotalElements(userPage.getTotalElements());
        response.setMeta(meta);

        return response;
    }

    @PreAuthorize("hasAuthority('SYSTEM_ADMINISTRATOR')")
    public UserResponse getUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToResponse(user);
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setStatus(user.getStatus());
        response.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()));

        List<UserStatusHistory> history = historyRepository.findByUserIdOrderByChangedAtDesc(user.getId());
        List<UserResponse.StatusHistorySummary> historySummaries = history.stream().map(h -> {
            UserResponse.StatusHistorySummary summary = new UserResponse.StatusHistorySummary();
            summary.setNewStatus(h.getNewStatus());
            summary.setReason(h.getReason());
            summary.setChangedAt(h.getChangedAt().toString());
            return summary;
        }).collect(Collectors.toList());

        response.setStatusHistory(historySummaries);
        return response;
    }

    @Transactional
    @PreAuthorize("hasAuthority('SYSTEM_ADMINISTRATOR')")
    public void changeUserStatus(String userId, com.ams.resident.dto.StatusRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        UserStatus oldStatus = user.getStatus();
        UserStatus newStatus = request.getStatus();
        
        if (oldStatus == newStatus) return;
        
        if ((newStatus == UserStatus.SUSPENDED || newStatus == UserStatus.DEACTIVATED) && 
            (request.getReason() == null || request.getReason().trim().isEmpty())) {
            throw new com.ams.resident.exception.BadRequestException("Reason is required for SUSPENDED or DEACTIVATED");
        }
        
        if (oldStatus == UserStatus.DEACTIVATED && newStatus == UserStatus.SUSPENDED) {
            throw new com.ams.resident.exception.BadRequestException("Invalid transition from DEACTIVATED to SUSPENDED");
        }
        
        user.setStatus(newStatus);
        userRepository.save(user);
        
        UserStatusHistory history = new UserStatusHistory();
        history.setUserId(userId);
        history.setPreviousStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setReason(request.getReason());
        history.setChangedBy(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName());
        historyRepository.save(history);
        
        auditService.logEvent("FR-AUD-002", history.getChangedBy(), "Changed status of user " + userId + " to " + newStatus);
    }
}


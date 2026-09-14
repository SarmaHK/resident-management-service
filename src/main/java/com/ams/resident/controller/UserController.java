package com.ams.resident.controller;

import com.ams.resident.dto.PaginatedUserResponse;
import com.ams.resident.dto.UserRequest;
import com.ams.resident.dto.UserResponse;
import com.ams.resident.entity.UserStatus;
import com.ams.resident.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse response = userService.createUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<PaginatedUserResponse> listUsers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.listUsers(query, status, role, page, size));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<Void> changeUserStatus(@PathVariable String userId, @Valid @RequestBody com.ams.resident.dto.StatusRequest request) {
        userService.changeUserStatus(userId, request);
        return ResponseEntity.ok().build();
    }
}

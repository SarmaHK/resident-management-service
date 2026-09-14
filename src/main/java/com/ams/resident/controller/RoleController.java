package com.ams.resident.controller;

import com.ams.resident.dto.RoleRequest;
import com.ams.resident.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/{userId}/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<Void> assignRole(@PathVariable String userId, @Valid @RequestBody RoleRequest request) {
        roleService.assignRole(userId, request.getRole());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{roleName}")
    public ResponseEntity<Void> removeRole(@PathVariable String userId, @PathVariable String roleName) {
        roleService.removeRole(userId, roleName);
        return ResponseEntity.ok().build();
    }
}

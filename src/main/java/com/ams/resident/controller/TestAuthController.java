package com.ams.resident.controller;

import com.ams.resident.entity.Role;
import com.ams.resident.entity.User;
import com.ams.resident.entity.UserStatus;
import com.ams.resident.repository.RoleRepository;
import com.ams.resident.repository.UserRepository;
import com.ams.resident.security.JwtUtils;
import com.ams.resident.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class TestAuthController {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @GetMapping("/test-token")
    public ResponseEntity<String> getTestAdminToken() {
        // 1. Create SYSTEM_ADMINISTRATOR role if it doesn't exist
        Role adminRole = roleRepository.findByName("SYSTEM_ADMINISTRATOR").orElseGet(() -> {
            Role r = new Role();
            r.setName("SYSTEM_ADMINISTRATOR");
            return roleRepository.save(r);
        });

        // 2. Create a test admin user if it doesn't exist
        User testAdmin = userRepository.findByEmail("admin@test.com").orElseGet(() -> {
            User u = new User();
            u.setFirstName("Test");
            u.setLastName("Admin");
            u.setEmail("admin@test.com");
            u.setStatus(UserStatus.ACTIVE);
            u.getRoles().add(adminRole);
            return userRepository.save(u);
        });

        // 3. Generate a valid JWT token
        UserDetailsImpl userDetails = UserDetailsImpl.build(testAdmin);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        
        String token = jwtUtils.generateJwtToken(auth);

        return ResponseEntity.ok(token);
    }
}

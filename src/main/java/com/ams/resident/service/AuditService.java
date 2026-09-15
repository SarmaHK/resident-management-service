package com.ams.resident.service;

import org.springframework.stereotype.Service;

@Service
public class AuditService {
    
    public void logEvent(String eventCode, String userId, String details) {
        // Shared Audit Stub
        System.out.println("AUDIT [" + eventCode + "] - User: " + userId + " - Details: " + details);
    }
}

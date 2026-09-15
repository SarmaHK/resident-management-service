package com.ams.resident.controller;

import com.ams.resident.dto.RelationshipRequest;
import com.ams.resident.dto.RelationshipResponse;
import com.ams.resident.service.RelationshipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/relationships")
@RequiredArgsConstructor
public class RelationshipController {

    private final RelationshipService relationshipService;

    @PostMapping
    public ResponseEntity<RelationshipResponse> createRelationshipRequest(@Valid @RequestBody RelationshipRequest request) {
        RelationshipResponse response = relationshipService.createRelationshipRequest(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public ResponseEntity<List<RelationshipResponse>> getOwnRelationships() {
        return ResponseEntity.ok(relationshipService.getOwnRelationships());
    }
}

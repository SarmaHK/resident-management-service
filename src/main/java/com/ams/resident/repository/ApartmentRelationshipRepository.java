package com.ams.resident.repository;

import com.ams.resident.entity.ApartmentRelationship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApartmentRelationshipRepository extends JpaRepository<ApartmentRelationship, String> {
    List<ApartmentRelationship> findByUserId(String userId);
}

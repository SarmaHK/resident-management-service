package com.ams.resident.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "apartment_relationships")
@Getter
@Setter
public class ApartmentRelationship {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String userId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RelationshipType relationshipType;
    
    @Column(nullable = false)
    private String unitReference;
    
    private String supportingInfo;
    
    @Enumerated(EnumType.STRING)
    private RelationshipStatus status = RelationshipStatus.PENDING;
    
    private String decisionReason;
}

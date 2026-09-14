package com.ams.resident.dto;

import com.ams.resident.entity.RelationshipStatus;
import com.ams.resident.entity.RelationshipType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RelationshipResponse {
    private String relationshipId;
    private RelationshipType relationshipType;
    private String unitReference;
    private RelationshipStatus status;
    private String decisionReason;
}

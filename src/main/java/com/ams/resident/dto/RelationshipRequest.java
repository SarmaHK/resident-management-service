package com.ams.resident.dto;

import com.ams.resident.entity.RelationshipType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RelationshipRequest {
    @NotNull
    private RelationshipType relationshipType;
    
    @NotBlank
    private String unitReference;
    
    private String supportingInfo;
}

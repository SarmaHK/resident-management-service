package com.ams.resident.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class PaginatedUserResponse {
    private List<UserResponse> data;
    private PageMetadata meta;
    
    @Getter
    @Setter
    public static class PageMetadata {
        private int page;
        private int size;
        private long totalElements;
    }
}

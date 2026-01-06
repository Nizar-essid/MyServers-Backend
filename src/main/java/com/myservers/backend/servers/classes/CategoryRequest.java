package com.myservers.backend.servers.classes;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequest {
    private String name;
    private String description;
    private Long parentId;
    private Boolean isActive;
    private Integer sortOrder;
}


package com.myservers.backend.servers.classes;

import com.myservers.backend.servers.entities.CategoryType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequest {
    private CategoryType type;
    private String name;
    private String description;
    private Long parentId;
    private Boolean isActive;
    private Integer sortOrder;
}


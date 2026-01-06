package com.myservers.backend.servers.classes;

import lombok.*;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private String parentName;
    private List<CategoryResponse> children;
    private Date dateCreation;
    private Date latestUpdate;
    private Integer createdById;
    private Integer updatedById;
    private Boolean isActive;
    private Integer sortOrder;
    private Long childrenCount;
    private Boolean hasChildren;
}


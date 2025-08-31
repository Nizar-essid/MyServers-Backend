package com.myservers.backend.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGroupDto {
    private Long id;
    private String name;
    private String description;
    private Double discountPercentage;
    private Boolean isActive;
}

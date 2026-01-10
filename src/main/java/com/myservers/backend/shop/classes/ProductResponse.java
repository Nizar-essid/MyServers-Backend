package com.myservers.backend.shop.classes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String image;
    private Boolean isAvailable;
    private Boolean isActive;
    private Date dateCreation;
    private Date latestUpdate;
    private Long createdById;
    private Long updatedById;
}


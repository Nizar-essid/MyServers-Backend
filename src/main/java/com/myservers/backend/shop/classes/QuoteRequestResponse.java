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
public class QuoteRequestResponse {
    private Long id;
    private ProductResponse product;
    private Long userId;
    private String userName;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private String message;
    private String status;
    private Date dateCreation;
    private Date dateProcessed;
    private Long processedById;
    private String processedByName;
    private String adminNotes;
}


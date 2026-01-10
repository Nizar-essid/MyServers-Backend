package com.myservers.backend.shop.classes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteRequestRequest {
    private Long productId;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private String message;
}


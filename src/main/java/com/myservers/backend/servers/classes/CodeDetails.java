package com.myservers.backend.servers.classes;

import lombok.*;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CodeDetails{
private Integer id;
    private String value;
    private float subscription_duration;
    private float price;
    private Float cost;
}

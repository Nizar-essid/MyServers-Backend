package com.myservers.backend.servers.classes;

import lombok.*;

import java.util.Date;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class addCodeDetails {
    private Integer id;
    public  String value;
    public long price;
    public long subscription_duration;
    private Date validUntil;}

package com.myservers.backend.servers.classes;

import lombok.*;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
@Builder
public class CodeType {
    private Integer duration;
    private Float price;
    private Float originalPrice;
    private Float discountPercentage;
    private String server_name;
   AllServersResponse server;
    private String code_value;
    private String checksum;
    private Date dateOfPurchase;
    private Integer id;
    private Date valid_until;
}

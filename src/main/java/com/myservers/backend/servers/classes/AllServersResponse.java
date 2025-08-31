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
public class AllServersResponse {

    private Long id;
    private String name_serv;
    private Date date_creation;
    private boolean state;
    private String logo;
    private List<CodeType> codes;
    private String serverType;
    private String description;
    private Double price;
    private Integer duration_months;
    private Boolean active;


}

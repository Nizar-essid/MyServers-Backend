package com.myservers.backend.servers.classes;

import com.myservers.backend.servers.entities.CodeState;
import lombok.*;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
@Builder
public class CodeBasicResponse {
    private Integer id;
    private Date date_creation;
    private Date latest_update;
    private Date purchased_on;
    private Date valid_until;
    private CodeState state;
    private Integer duration;
    private Float price;
}

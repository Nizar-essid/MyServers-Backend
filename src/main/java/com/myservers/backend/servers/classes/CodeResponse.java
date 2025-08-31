package com.myservers.backend.servers.classes;

import com.myservers.backend.security.auth.entities.Admin;
import com.myservers.backend.servers.entities.CodeState;
import com.myservers.backend.servers.entities.Server;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
@Builder
public class CodeResponse {
    private Integer id;
    private String code_value;
    private Date date_creation;
    private Date latest_update;
    private Date purchased_on;
    private Date valid_until;
    private CodeState state;
    private Integer duration;
    private Float price;
}

package com.myservers.backend.servers.classes;
import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddServerDetails {
    private Integer id;
    public String name_serv;
    public String logo;
    private Long categoryId;
}


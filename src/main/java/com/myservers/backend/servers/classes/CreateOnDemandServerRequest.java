package com.myservers.backend.servers.classes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOnDemandServerRequest {

    @JsonProperty("name_serv")
    public String name_serv;

    @JsonProperty("description")
    public String description;

    @JsonProperty("price")
    public Double price;

    @JsonProperty("duration_months")
    public Integer duration_months;

    @JsonProperty("active")
    public Boolean active;

    @JsonProperty("serverType")
    public String serverType;

    @JsonProperty("logo")
    public String logo;

    @JsonProperty("categoryId")
    public Long categoryId;
}

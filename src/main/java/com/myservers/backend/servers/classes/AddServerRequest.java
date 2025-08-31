package com.myservers.backend.servers.classes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddServerRequest {

    @JsonProperty("server_details")
    public AddServerDetails server_details;
    public List<addCodeDetails> codes_added;

    public String getDetails(){
        return this.getServer_details().getLogo()+this.getServer_details().getName_serv()+this.codes_added.toString();

    }
}

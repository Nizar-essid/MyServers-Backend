package com.myservers.backend.servers.responsesDataType;

import com.myservers.backend.servers.classes.AllServersResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor


public class AllServersRequestResponse {

    private Integer status=200;
    private String message="success";
    private List<AllServersResponse> data;
}

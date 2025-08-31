package com.myservers.backend.security.auth.dataTypes;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AuthenticationResponse {
    private String token;
    private Integer status=200;
    private String message="success";
    private String secretImageUri;
    private boolean isMfaEnabled;
}

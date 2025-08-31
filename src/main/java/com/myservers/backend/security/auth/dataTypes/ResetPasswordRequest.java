package com.myservers.backend.security.auth.dataTypes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest  {
    private String email;
    private String  password;
    private String secret;

}



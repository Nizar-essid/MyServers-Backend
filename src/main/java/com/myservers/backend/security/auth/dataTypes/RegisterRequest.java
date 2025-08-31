package com.myservers.backend.security.auth.dataTypes;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String firstname;
    private String lastName;
    private String email;
    private String  password;
    private boolean mfaEnabled;
    private String secret;
}

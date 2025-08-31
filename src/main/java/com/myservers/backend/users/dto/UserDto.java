package com.myservers.backend.users.dto;

import com.myservers.backend.security.auth.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Integer id;
    private String firstname;
    private String lastname;
    private String email;
    private int telephone;
    private float balance;
    private boolean state;
    private String role;
    private boolean mfaEnabled;
}

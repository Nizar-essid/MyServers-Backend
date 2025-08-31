package com.myservers.backend.users.classes;

import com.myservers.backend.security.auth.entities.Role;
import lombok.*;


@Getter
@Setter
@Builder

public class UserResponse {
    private  Integer id;
    private String firstname;
    private String lastname;
    private String email;
    private int telephone;
    private boolean state;
    private float balance;
    private Role role;

    @Override
    public String toString() {
        return "UserResponse{" +
                "id=" + id +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", email='" + email + '\'' +
                ", telephone=" + telephone +
                ", state=" + state +
                ", balance=" + balance +
                ", role=" + role +
                '}';
    }
}

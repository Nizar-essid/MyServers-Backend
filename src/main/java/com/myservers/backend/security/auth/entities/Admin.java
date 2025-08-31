package com.myservers.backend.security.auth.entities;

import com.myservers.backend.users.entities.UserGroup;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@DiscriminatorValue("ADMIN")

public class Admin extends User {
    public Admin() {
        super();
        this.setRole(Role.ADMIN);
    }

    public Admin(Integer id, String firstname, String lastname, String email, String password, int telephone, boolean state, Date date_creation, Date date_latest_update, float balance, String verification_code, Role role,boolean mfaEnabled,String secret,String passwordResetToken, List<UserGroup> userGroups){
        super(id, firstname, lastname, email, password, telephone, state, date_creation, date_latest_update, balance, verification_code, role,mfaEnabled,secret,passwordResetToken, userGroups);
        this.setRole(Role.ADMIN);
    }
}

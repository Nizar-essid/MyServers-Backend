package com.myservers.backend.servers.classes;
import com.myservers.backend.servers.entities.SubscrptionState;
import com.myservers.backend.users.classes.UserResponse;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
@Builder
public class  Purchase {

    private Integer id;
    private SubscrptionState  state;
    private UserResponse user;
    private CodeType Code;
    private float price_after_discount;
}

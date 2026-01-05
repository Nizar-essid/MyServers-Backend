package com.myservers.backend.servers.entities;

import com.myservers.backend.security.auth.entities.Admin;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Builder
@Entity
@Table(name = "code")
@NoArgsConstructor
@AllArgsConstructor
public class Code {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    private String code_value;
    @Column(name = "date_creation", nullable = false)
    private Date dateCreation;
    @Column(name = "latest_update", nullable = true)
    private Date lastest_Update;
    @Column(name = "purchased_on", nullable = true)
    private Date purchasedOn;
    @Enumerated(EnumType.STRING)
    private CodeState state;
    @Column(name = "subscription_duration", nullable = false)
    private Integer subscriptionDuration;
    private Float price;
    @Column(name = "cost", nullable = true)
    private Float cost;
    @ManyToOne
    @JoinColumn(name = "origin_server_id")
    private Server originServer;
    @ManyToOne
    @JoinColumn(name = "added_by_id_user")
    private Admin added_by;
    @ManyToOne
    @JoinColumn(name = "updated_by_id_user")
    private Admin updated_by;
//    private TypeCode type_subscription;
}

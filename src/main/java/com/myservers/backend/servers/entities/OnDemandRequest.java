package com.myservers.backend.servers.entities;

import com.myservers.backend.security.auth.entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Builder
@Entity
@Table(name = "on_demand_requests")
@AllArgsConstructor
public class OnDemandRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "server_id")
    private Server server;

    @Column(name = "mac_address", nullable = false)
    private String macAddress;

    @Column(name = "device_key", nullable = false)
    private String deviceKey;

    @Column(name = "price", nullable = false)
    private Float price;

    @Column(name = "price_after_discount")
    private Float priceAfterDiscount;

    @Column(name = "duration_months", nullable = false)
    private Integer durationMonths;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "request_date")
    private Date requestDate;

    @Column(name = "processed_date")
    private Date processedDate;

    @ManyToOne
    @JoinColumn(name = "processed_by")
    private com.myservers.backend.security.auth.entities.Admin processedBy;

    @Column(name = "admin_notes")
    private String adminNotes;

    public OnDemandRequest(User user, Server server, String macAddress, String deviceKey,
                          Float price, Float priceAfterDiscount, Integer durationMonths) {
        this.user = user;
        this.server = server;
        this.macAddress = macAddress;
        this.deviceKey = deviceKey;
        this.price = price;
        this.priceAfterDiscount = priceAfterDiscount;
        this.durationMonths = durationMonths;
        this.requestDate = new Date();
    }
}

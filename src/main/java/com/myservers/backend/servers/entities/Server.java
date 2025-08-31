package com.myservers.backend.servers.entities;

import com.myservers.backend.security.auth.entities.Admin;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Builder
@Entity
@Table(name = "servers")
@AllArgsConstructor
public class Server {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    private String name_serv;
    private Date date_creation;
    private boolean state;
    private String logo;
    @ManyToOne
    @JoinColumn(name = "added_by_id_user")
    private Admin added_by;
    @ManyToOne
    @JoinColumn(name = "updated_by_id_user")
    private Admin updated_by ;

    @Enumerated(EnumType.STRING)
    @Column(name = "server_type")
    private ServerType serverType = ServerType.CODE_BASED; // Default to code-based

    private String description;
    private Double price;
    private Integer duration_months;
    private Boolean active;

    public Server(Admin added_by, String name_serv, Date date_creation, boolean state, String logo, Admin updated_by) {
        this.added_by = added_by;
        this.name_serv = name_serv;
        this.date_creation = date_creation;
        this.state = state;
        this.logo = logo;
        this.updated_by = updated_by;
    }

    public Server(Admin added_by, String name_serv, Date date_creation, boolean state, String logo, Admin updated_by,
                  String description, Double price, Integer duration_months, Boolean active, ServerType serverType) {
        this.added_by = added_by;
        this.name_serv = name_serv;
        this.date_creation = date_creation;
        this.state = state;
        this.logo = logo;
        this.updated_by = updated_by;
        this.description = description;
        this.price = price;
        this.duration_months = duration_months;
        this.active = active;
        this.serverType = serverType;
    }
}

package com.myservers.backend.servers.classes;

import com.myservers.backend.servers.entities.RequestStatus;
import lombok.*;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
@Builder
public class OnDemandRequestResponse {
    private Long id;
    private Long userId;
    private String userEmail;
    private String userName;
    private Long serverId;
    private String serverName;
    private String serverLogo;
    private String macAddress;
    private String deviceKey;
    private Float price;
    private Float priceAfterDiscount;
    private Integer durationMonths;
    private RequestStatus status;
    private Date requestDate;
    private Date processedDate;
    private String adminNotes;
    private String processedByEmail;
}

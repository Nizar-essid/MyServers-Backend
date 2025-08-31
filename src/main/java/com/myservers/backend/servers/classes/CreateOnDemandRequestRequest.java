package com.myservers.backend.servers.classes;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
@Builder
public class CreateOnDemandRequestRequest {
    private Long serverId;
    private String macAddress;
    private String deviceKey;
    private Integer durationMonths;
}

package com.myservers.backend.servers.classes;

import com.myservers.backend.servers.entities.RequestStatus;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
@Builder
public class ProcessOnDemandRequestRequest {
    private Long requestId;
    private RequestStatus status;
    private String adminNotes;
}

package com.myservers.backend.statistics.classes;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Builder
@Data
public class GetStatisticsRequest {
    private String period;
}

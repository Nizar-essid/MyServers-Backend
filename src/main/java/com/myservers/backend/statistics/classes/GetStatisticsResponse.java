package com.myservers.backend.statistics.classes;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetStatisticsResponse {
    private Integer status = 200;
    private String message = "success";
    private String period;
    private Statistics data;

}

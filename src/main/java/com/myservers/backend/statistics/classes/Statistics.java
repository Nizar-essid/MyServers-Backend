package com.myservers.backend.statistics.classes;

import lombok.Builder;
import lombok.Data;

import java.util.List;
@Builder
@Data
public class Statistics {
    private List<String> labels;
    private List<Integer> userData;
    private List<Double>  purchaseData;
    private List<Integer> serverData;
    private List<Double>  balanceData;
}

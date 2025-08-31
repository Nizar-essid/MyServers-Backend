package com.myservers.backend.statistics.controllers;


import com.myservers.backend.statistics.classes.GetStatisticsRequest;
import com.myservers.backend.statistics.classes.GetStatisticsResponse;
import com.myservers.backend.statistics.services.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.json.ParseException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@CrossOrigin(origins = "*", exposedHeaders = "**")

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/statistics/admin")
public class StatisticsController {
    private final StatisticsService statisticsService;

    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @PostMapping(value = "/get_statistics",consumes = MediaType.APPLICATION_JSON_VALUE)
    public GetStatisticsResponse getStatistics(@RequestBody HashMap<String,Object> requestBody)throws ParseException {
    String period = (String) requestBody.get("period");

        return statisticsService.getStatistics(period);
}}

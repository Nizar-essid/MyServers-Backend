package com.myservers.backend.shop.controllers;

import com.myservers.backend.security.config.JwtService;
import com.myservers.backend.servers.classes.GeneralResponse;
import com.myservers.backend.shop.classes.QuoteRequestRequest;
import com.myservers.backend.shop.classes.QuoteRequestResponse;
import com.myservers.backend.shop.entities.QuoteRequestStatus;
import com.myservers.backend.shop.services.QuoteRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@CrossOrigin(origins = "*", exposedHeaders = "**")
@RestController
@RequestMapping("/api/v1/shop")
@RequiredArgsConstructor
public class QuoteRequestController {

    private final QuoteRequestService quoteRequestService;
    private final JwtService jwtService;

    @PostMapping(value = "/quote-request", consumes = MediaType.APPLICATION_JSON_VALUE)
    public GeneralResponse createQuoteRequest(@RequestBody QuoteRequestRequest request) {
        try {
            com.myservers.backend.security.auth.entities.User user = jwtService.getUser();
            QuoteRequestResponse quoteRequest = quoteRequestService.createQuoteRequest(request, user);
            ArrayList<Object> data = new ArrayList<>();
            data.add(quoteRequest);
            return GeneralResponse.builder()
                    .status(200L)
                    .result("Quote request created successfully")
                    .data(data)
                    .build();
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error: " + e.getMessage())
                    .build();
        }
    }

    @GetMapping("/admin/quote-requests")
    public GeneralResponse getAllQuoteRequests() {
        try {
            java.util.List<QuoteRequestResponse> requests = quoteRequestService.getAllQuoteRequests();
            return GeneralResponse.builder()
                    .status(200L)
                    .result("Success")
                    .data(new ArrayList<>(requests))
                    .build();
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error: " + e.getMessage())
                    .build();
        }
    }

    @GetMapping("/admin/quote-requests/status/{status}")
    public GeneralResponse getQuoteRequestsByStatus(@PathVariable String status) {
        try {
            QuoteRequestStatus requestStatus = QuoteRequestStatus.valueOf(status.toUpperCase());
            java.util.List<QuoteRequestResponse> requests = quoteRequestService.getQuoteRequestsByStatus(requestStatus);
            return GeneralResponse.builder()
                    .status(200L)
                    .result("Success")
                    .data(new ArrayList<>(requests))
                    .build();
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error: " + e.getMessage())
                    .build();
        }
    }

    @GetMapping("/quote-requests/my")
    public GeneralResponse getMyQuoteRequests() {
        try {
            com.myservers.backend.security.auth.entities.User user = jwtService.getUser();
            java.util.List<QuoteRequestResponse> requests = quoteRequestService.getQuoteRequestsByUser(user.getId());
            return GeneralResponse.builder()
                    .status(200L)
                    .result("Success")
                    .data(new ArrayList<>(requests))
                    .build();
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error: " + e.getMessage())
                    .build();
        }
    }

    @PostMapping(value = "/admin/quote-requests/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    public GeneralResponse updateQuoteRequestStatus(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> request) {
        try {
            com.myservers.backend.security.auth.entities.Admin admin = jwtService.getAdmin();
            QuoteRequestStatus status = QuoteRequestStatus.valueOf(request.get("status").toUpperCase());
            String adminNotes = request.get("adminNotes");
            QuoteRequestResponse updated = quoteRequestService.updateQuoteRequestStatus(id, status, adminNotes, admin);
            ArrayList<Object> data = new ArrayList<>();
            data.add(updated);
            return GeneralResponse.builder()
                    .status(200L)
                    .result("Quote request status updated successfully")
                    .data(data)
                    .build();
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error: " + e.getMessage())
                    .build();
        }
    }
}


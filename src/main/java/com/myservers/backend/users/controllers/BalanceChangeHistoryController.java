package com.myservers.backend.users.controllers;

import com.myservers.backend.users.entities.BalanceChangeHistory;
import com.myservers.backend.users.services.BalanceChangeHistoryService;
import com.myservers.backend.security.auth.entities.Admin;
import com.myservers.backend.security.auth.entities.User;
import com.myservers.backend.users.UserService.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/balance-history")
@CrossOrigin(origins = "*")
public class BalanceChangeHistoryController {

    @Autowired
    private BalanceChangeHistoryService balanceChangeHistoryService;

    @Autowired
    private UserService userService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<BalanceChangeHistory> historyPage = balanceChangeHistoryService.getAllActiveHistoryPaginated(pageable);

            return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "Balance change history retrieved successfully",
                "data", historyPage.getContent(),
                "totalElements", historyPage.getTotalElements(),
                "totalPages", historyPage.getTotalPages(),
                "currentPage", historyPage.getNumber()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "message", "Error retrieving balance change history: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getHistoryByUser(@PathVariable Integer userId) {
        try {
            List<BalanceChangeHistory> history = balanceChangeHistoryService.getHistoryByUserId(userId);
            return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "User balance change history retrieved successfully",
                "data", history
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "message", "Error retrieving user balance change history: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/payment-status/{status}")
    public ResponseEntity<?> getHistoryByPaymentStatus(@PathVariable String status) {
        try {
            BalanceChangeHistory.PaymentStatus paymentStatus = BalanceChangeHistory.PaymentStatus.valueOf(status.toUpperCase());
            List<BalanceChangeHistory> history = balanceChangeHistoryService.getHistoryByPaymentStatus(paymentStatus);
            return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "Balance change history by payment status retrieved successfully",
                "data", history
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "message", "Error retrieving balance change history by payment status: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/change-type/{type}")
    public ResponseEntity<?> getHistoryByChangeType(@PathVariable String type) {
        try {
            BalanceChangeHistory.ChangeType changeType = BalanceChangeHistory.ChangeType.valueOf(type.toUpperCase());
            List<BalanceChangeHistory> history = balanceChangeHistoryService.getHistoryByChangeType(changeType);
            return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "Balance change history by change type retrieved successfully",
                "data", history
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "message", "Error retrieving balance change history by change type: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchHistory(@RequestParam String searchTerm) {
        try {
            List<BalanceChangeHistory> history = balanceChangeHistoryService.searchHistoryByUser(searchTerm);
            return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "Balance change history search completed successfully",
                "data", history
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "message", "Error searching balance change history: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createBalanceChange(
            Authentication authentication,
            @RequestBody Map<String, Object> request
    ) {
        try {
            Integer userId;
            Object userIdObj = request.get("userId");
            if (userIdObj instanceof Integer) {
                userId = (Integer) userIdObj;
            } else if (userIdObj instanceof String) {
                userId = Integer.parseInt((String) userIdObj);
            } else if (userIdObj instanceof Number) {
                userId = ((Number) userIdObj).intValue();
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "message", "Invalid userId format"
                ));
            }

            Double amount = ((Number) request.get("amount")).doubleValue();
            String changeTypeStr = (String) request.get("changeType");
            String paymentStatusStr = (String) request.get("paymentStatus");
            String description = (String) request.get("description");

            if (userId == null || amount == null || changeTypeStr == null || paymentStatusStr == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "message", "Missing required fields: userId, amount, changeType, paymentStatus"
                ));
            }

            User user = userService.getUserId(userId);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "message", "User not found"
                ));
            }

            // Get current admin from authentication
            User currentUser = userService.getUserByEmail(authentication.getName());
            if (currentUser == null || !(currentUser instanceof Admin)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "message", "Admin authentication required"
                ));
            }

            Admin admin = (Admin) currentUser;
            BalanceChangeHistory.ChangeType changeType = BalanceChangeHistory.ChangeType.valueOf(changeTypeStr.toUpperCase());
            BalanceChangeHistory.PaymentStatus paymentStatus = BalanceChangeHistory.PaymentStatus.valueOf(paymentStatusStr.toUpperCase());

            BalanceChangeHistory history = balanceChangeHistoryService.createBalanceChange(
                user, admin, amount, changeType, paymentStatus, description
            );

            return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "Balance change created successfully",
                "data", history
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "message", "Error creating balance change: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{historyId}/payment-status")
    public ResponseEntity<?> updatePaymentStatus(
            @PathVariable Long historyId,
            @RequestBody Map<String, String> request
    ) {
        try {
            String newStatusStr = request.get("paymentStatus");
            if (newStatusStr == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "message", "Payment status is required"
                ));
            }

            BalanceChangeHistory.PaymentStatus newStatus = BalanceChangeHistory.PaymentStatus.valueOf(newStatusStr.toUpperCase());
            BalanceChangeHistory updatedHistory = balanceChangeHistoryService.updatePaymentStatus(historyId, newStatus);

            return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "Payment status updated successfully",
                "data", updatedHistory
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "message", "Error updating payment status: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{historyId}/cancel")
    public ResponseEntity<?> cancelHistory(
            @PathVariable Long historyId,
            @RequestBody Map<String, String> request
    ) {
        try {
            String cancellationReason = request.get("cancellationReason");
            if (cancellationReason == null || cancellationReason.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "message", "Cancellation reason is required"
                ));
            }

            BalanceChangeHistory cancelledHistory = balanceChangeHistoryService.cancelHistory(historyId, cancellationReason);
            return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "Balance change history cancelled successfully",
                "data", cancelledHistory
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "message", "Error cancelling balance change history: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        try {
            Long unpaidCount = balanceChangeHistoryService.getUnpaidChangesCount();
            Long paidCount = balanceChangeHistoryService.getPaidChangesCount();
            Double unpaidAmount = balanceChangeHistoryService.getUnpaidAmountsSum();
            Double paidAmount = balanceChangeHistoryService.getPaidAmountsSum();

            return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "Statistics retrieved successfully",
                "data", Map.of(
                    "unpaidCount", unpaidCount,
                    "paidCount", paidCount,
                    "unpaidAmount", unpaidAmount,
                    "paidAmount", paidAmount
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "message", "Error retrieving statistics: " + e.getMessage()
            ));
        }
    }
}

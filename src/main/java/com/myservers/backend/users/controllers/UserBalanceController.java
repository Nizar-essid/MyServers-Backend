package com.myservers.backend.users.controllers;

import com.myservers.backend.users.entities.UserBalance;
import com.myservers.backend.users.services.UserBalanceService;
import com.myservers.backend.security.auth.entities.User;
import com.myservers.backend.users.UserService.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users/balances")
@CrossOrigin(origins = "*")
public class UserBalanceController {

    @Autowired
    private UserBalanceService userBalanceService;

    @Autowired
    private UserService userService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllBalances() {
        try {
            List<UserBalance> balances = userBalanceService.getAllActiveBalances();
            return ResponseEntity.ok().body(Map.of(
                "status", 200,
                "message", "User balances retrieved successfully",
                "data", balances
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "message", "Error retrieving user balances: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/positive")
    public ResponseEntity<?> getUsersWithPositiveBalance() {
        try {
            List<UserBalance> balances = userBalanceService.getUsersWithPositiveBalance();
            return ResponseEntity.ok().body(Map.of(
                "status", 200,
                "message", "Users with positive balance retrieved successfully",
                "data", balances
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "message", "Error retrieving users with positive balance: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getBalanceByUserId(@PathVariable Long userId) {
        try {
            Optional<UserBalance> balance = userBalanceService.getBalanceByUserId(userId);
            if (balance.isPresent()) {
                return ResponseEntity.ok().body(Map.of(
                    "status", 200,
                    "message", "User balance retrieved successfully",
                    "data", balance.get()
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "message", "Error retrieving user balance: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/my-balance")
    public ResponseEntity<?> getMyBalance(Authentication authentication) {
        try {
            User user = userService.getUserByEmail(authentication.getName());
            Optional<UserBalance> balance = userBalanceService.getBalanceByUser(user);
            if (balance.isPresent()) {
                return ResponseEntity.ok().body(Map.of(
                    "status", 200,
                    "message", "Balance retrieved successfully",
                    "data", balance.get()
                ));
            } else {
                // Create balance if it doesn't exist
                UserBalance newBalance = userBalanceService.createBalance(user);
                return ResponseEntity.ok().body(Map.of(
                    "status", 200,
                    "message", "Balance created and retrieved successfully",
                    "data", newBalance
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "message", "Error retrieving balance: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/add/{userId}")
    public ResponseEntity<?> addBalance(@PathVariable Long userId, @RequestBody Map<String, Double> request) {
        try {
            Double amount = request.get("amount");
            if (amount == null || amount <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "message", "Invalid amount. Amount must be greater than 0."
                ));
            }

            UserBalance updatedBalance = userBalanceService.addBalance(userId, amount);
            return ResponseEntity.ok().body(Map.of(
                "status", 200,
                "message", "Balance added successfully",
                "data", updatedBalance
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "message", "Error adding balance: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/deduct/{userId}")
    public ResponseEntity<?> deductBalance(@PathVariable Long userId, @RequestBody Map<String, Double> request) {
        try {
            Double amount = request.get("amount");
            if (amount == null || amount <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "message", "Invalid amount. Amount must be greater than 0."
                ));
            }

            UserBalance updatedBalance = userBalanceService.deductBalance(userId, amount);
            return ResponseEntity.ok().body(Map.of(
                "status", 200,
                "message", "Balance deducted successfully",
                "data", updatedBalance
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "message", "Error deducting balance: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/update/{userId}")
    public ResponseEntity<?> updateBalance(@PathVariable Long userId, @RequestBody Map<String, Double> request) {
        try {
            Double newBalance = request.get("balance");
            if (newBalance == null || newBalance < 0) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "message", "Invalid balance. Balance must be greater than or equal to 0."
                ));
            }

            UserBalance updatedBalance = userBalanceService.updateBalance(userId, newBalance);
            return ResponseEntity.ok().body(Map.of(
                "status", 200,
                "message", "Balance updated successfully",
                "data", updatedBalance
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "message", "Error updating balance: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/check-balance/{userId}")
    public ResponseEntity<?> checkBalance(@PathVariable Long userId, @RequestParam Double amount) {
        try {
            boolean hasSufficientBalance = userBalanceService.hasSufficientBalance(userId, amount);
            Double currentBalance = userBalanceService.getCurrentBalance(userId);

            return ResponseEntity.ok().body(Map.of(
                "status", 200,
                "message", "Balance check completed",
                "data", Map.of(
                    "hasSufficientBalance", hasSufficientBalance,
                    "currentBalance", currentBalance,
                    "requiredAmount", amount
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "message", "Error checking balance: " + e.getMessage()
            ));
        }
    }
}

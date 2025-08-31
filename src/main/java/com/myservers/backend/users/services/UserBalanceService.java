package com.myservers.backend.users.services;

import com.myservers.backend.users.entities.UserBalance;
import com.myservers.backend.users.repositories.UserBalanceRepository;
import com.myservers.backend.security.auth.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserBalanceService {

    @Autowired
    private UserBalanceRepository userBalanceRepository;

    public List<UserBalance> getAllActiveBalances() {
        return userBalanceRepository.findAllActiveBalances();
    }

    public List<UserBalance> getUsersWithPositiveBalance() {
        return userBalanceRepository.findUsersWithPositiveBalance();
    }

    public Optional<UserBalance> getBalanceByUser(User user) {
        return userBalanceRepository.findByUser(user);
    }

    public Optional<UserBalance> getBalanceByUserId(Long userId) {
        return userBalanceRepository.findByUserId(userId);
    }

    public UserBalance createBalance(User user) {
        UserBalance balance = UserBalance.builder()
                .user(user)
                .balance(0.0)
                .totalDeposited(0.0)
                .totalSpent(0.0)
                .lastUpdated(new Date())
                .isActive(true)
                .build();
        return userBalanceRepository.save(balance);
    }

    public UserBalance addBalance(Long userId, Double amount) {
        Optional<UserBalance> balanceOpt = userBalanceRepository.findByUserId(userId);
        if (balanceOpt.isPresent()) {
            UserBalance balance = balanceOpt.get();
            balance.setBalance(balance.getBalance() + amount);
            balance.setTotalDeposited(balance.getTotalDeposited() + amount);
            balance.setLastUpdated(new Date());
            return userBalanceRepository.save(balance);
        }
        throw new RuntimeException("User balance not found for user id: " + userId);
    }

    public UserBalance deductBalance(Long userId, Double amount) {
        Optional<UserBalance> balanceOpt = userBalanceRepository.findByUserId(userId);
        if (balanceOpt.isPresent()) {
            UserBalance balance = balanceOpt.get();
            if (balance.getBalance() >= amount) {
                balance.setBalance(balance.getBalance() - amount);
                balance.setTotalSpent(balance.getTotalSpent() + amount);
                balance.setLastUpdated(new Date());
                return userBalanceRepository.save(balance);
            } else {
                throw new RuntimeException("Insufficient balance for user id: " + userId);
            }
        }
        throw new RuntimeException("User balance not found for user id: " + userId);
    }

    public UserBalance updateBalance(Long userId, Double newBalance) {
        Optional<UserBalance> balanceOpt = userBalanceRepository.findByUserId(userId);
        if (balanceOpt.isPresent()) {
            UserBalance balance = balanceOpt.get();
            balance.setBalance(newBalance);
            balance.setLastUpdated(new Date());
            return userBalanceRepository.save(balance);
        }
        throw new RuntimeException("User balance not found for user id: " + userId);
    }

    public boolean hasSufficientBalance(Long userId, Double amount) {
        Optional<UserBalance> balanceOpt = userBalanceRepository.findByUserId(userId);
        return balanceOpt.map(balance -> balance.getBalance() >= amount).orElse(false);
    }

    public Double getCurrentBalance(Long userId) {
        Optional<UserBalance> balanceOpt = userBalanceRepository.findByUserId(userId);
        return balanceOpt.map(UserBalance::getBalance).orElse(0.0);
    }
}

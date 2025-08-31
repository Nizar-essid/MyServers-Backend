package com.myservers.backend.users.services;

import com.myservers.backend.users.entities.BalanceChangeHistory;
import com.myservers.backend.users.entities.UserBalance;
import com.myservers.backend.users.repositories.BalanceChangeHistoryRepository;
import com.myservers.backend.users.repositories.UserBalanceRepository;
import com.myservers.backend.security.auth.entities.Admin;
import com.myservers.backend.security.auth.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class BalanceChangeHistoryService {

    @Autowired
    private BalanceChangeHistoryRepository balanceChangeHistoryRepository;

    @Autowired
    private UserBalanceRepository userBalanceRepository;

    @Autowired
    private com.myservers.backend.security.auth.repositories.UserRepository userRepository;

    public List<BalanceChangeHistory> getAllActiveHistory() {
        return balanceChangeHistoryRepository.findAll().stream()
                .sorted((a, b) -> b.getChangeDate().compareTo(a.getChangeDate()))
                .toList();
    }

    public Page<BalanceChangeHistory> getAllActiveHistoryPaginated(Pageable pageable) {
        return balanceChangeHistoryRepository.findAllOrderByChangeDateDesc(pageable);
    }

    public List<BalanceChangeHistory> getHistoryByUser(User user) {
        return balanceChangeHistoryRepository.findByUserAndIsActiveOrderByChangeDateDesc(user, true);
    }

    public List<BalanceChangeHistory> getHistoryByUserId(Integer userId) {
        return balanceChangeHistoryRepository.findByUserIdOrderByChangeDateDesc(userId);
    }

    public List<BalanceChangeHistory> getHistoryByAdmin(Admin admin) {
        return balanceChangeHistoryRepository.findByAdminIdOrderByChangeDateDesc(admin.getId());
    }

    public List<BalanceChangeHistory> getHistoryByPaymentStatus(BalanceChangeHistory.PaymentStatus paymentStatus) {
        return balanceChangeHistoryRepository.findByPaymentStatusOrderByChangeDateDesc(paymentStatus);
    }

    public List<BalanceChangeHistory> getHistoryByChangeType(BalanceChangeHistory.ChangeType changeType) {
        return balanceChangeHistoryRepository.findByChangeTypeOrderByChangeDateDesc(changeType);
    }

    public List<BalanceChangeHistory> getHistoryByDateRange(Date startDate, Date endDate) {
        return balanceChangeHistoryRepository.findByChangeDateBetweenOrderByChangeDateDesc(startDate, endDate);
    }

    public List<BalanceChangeHistory> searchHistoryByUser(String searchTerm) {
        return balanceChangeHistoryRepository.findByUserEmailOrNameContainingOrderByChangeDateDesc(searchTerm);
    }

    public BalanceChangeHistory createBalanceChange(
            User user,
            Admin admin,
            Double amount,
            BalanceChangeHistory.ChangeType changeType,
            BalanceChangeHistory.PaymentStatus paymentStatus,
            String description
    ) {
        // For system-generated entries (like initial balance), admin can be null
        if (admin == null) {
            // Create a system-generated entry without admin
            return createSystemBalanceChange(user, amount, changeType, paymentStatus, description);
        }

        // Get current user balance
        Optional<UserBalance> currentBalanceOpt = userBalanceRepository.findByUser(user);
        Double previousBalance = 0.0;
        Double newBalance = 0.0;

        if (currentBalanceOpt.isPresent()) {
            previousBalance = currentBalanceOpt.get().getBalance();
        }

        // Calculate new balance based on change type
        switch (changeType) {
            case ADD:
                newBalance = previousBalance + amount;
                break;
            case SET:
                newBalance = amount;
                break;
        }

        // Create or update user balance
        UserBalance userBalance;
        if (currentBalanceOpt.isPresent()) {
            userBalance = currentBalanceOpt.get();
            userBalance.setBalance(newBalance);
            userBalance.setLastUpdated(new Date());
            if (changeType == BalanceChangeHistory.ChangeType.ADD) {
                userBalance.setTotalDeposited(userBalance.getTotalDeposited() + amount);
            }
        } else {
            userBalance = UserBalance.builder()
                    .user(user)
                    .balance(newBalance)
                    .totalDeposited(changeType == BalanceChangeHistory.ChangeType.ADD ? amount : 0.0)
                    .totalSpent(0.0)
                    .lastUpdated(new Date())
                    .isActive(true)
                    .build();
        }
        userBalanceRepository.save(userBalance);

        // Update user balance in users table
        user.setBalance(newBalance.floatValue());
        userRepository.save(user);

        // Create balance change history record
        BalanceChangeHistory history = BalanceChangeHistory.builder()
                .user(user)
                .admin(admin)
                .amount(amount)
                .previousBalance(previousBalance)
                .newBalance(newBalance)
                .changeType(changeType)
                .paymentStatus(paymentStatus)
                .description(description)
                .changeDate(new Date())
                .isActive(true)
                .build();

        return balanceChangeHistoryRepository.save(history);
    }

    private BalanceChangeHistory createSystemBalanceChange(
            User user,
            Double amount,
            BalanceChangeHistory.ChangeType changeType,
            BalanceChangeHistory.PaymentStatus paymentStatus,
            String description
    ) {
        // Get current user balance
        Optional<UserBalance> currentBalanceOpt = userBalanceRepository.findByUser(user);
        Double previousBalance = 0.0;
        Double newBalance = 0.0;

        if (currentBalanceOpt.isPresent()) {
            previousBalance = currentBalanceOpt.get().getBalance();
        }

        // Calculate new balance based on change type
        switch (changeType) {
            case ADD:
                newBalance = previousBalance + amount;
                break;
            case SET:
                newBalance = amount;
                break;
        }

        // Create or update user balance
        UserBalance userBalance;
        if (currentBalanceOpt.isPresent()) {
            userBalance = currentBalanceOpt.get();
            userBalance.setBalance(newBalance);
            userBalance.setLastUpdated(new Date());
            if (changeType == BalanceChangeHistory.ChangeType.ADD) {
                userBalance.setTotalDeposited(userBalance.getTotalDeposited() + amount);
            }
        } else {
            userBalance = UserBalance.builder()
                    .user(user)
                    .balance(newBalance)
                    .totalDeposited(changeType == BalanceChangeHistory.ChangeType.ADD ? amount : 0.0)
                    .totalSpent(0.0)
                    .lastUpdated(new Date())
                    .isActive(true)
                    .build();
        }
        userBalanceRepository.save(userBalance);

        // Update user balance in users table
        user.setBalance(newBalance.floatValue());
        userRepository.save(user);

        // Create balance change history record (without admin)
        BalanceChangeHistory history = BalanceChangeHistory.builder()
                .user(user)
                .admin(null) // System-generated entry
                .amount(amount)
                .previousBalance(previousBalance)
                .newBalance(newBalance)
                .changeType(changeType)
                .paymentStatus(paymentStatus)
                .description(description)
                .changeDate(new Date())
                .isActive(true)
                .build();

        return balanceChangeHistoryRepository.save(history);
    }

    public BalanceChangeHistory updatePaymentStatus(Long historyId, BalanceChangeHistory.PaymentStatus newStatus) {
        Optional<BalanceChangeHistory> historyOpt = balanceChangeHistoryRepository.findById(historyId);
        if (historyOpt.isPresent()) {
            BalanceChangeHistory history = historyOpt.get();
            history.setPaymentStatus(newStatus);
            return balanceChangeHistoryRepository.save(history);
        }
        throw new RuntimeException("Balance change history not found with id: " + historyId);
    }

    public BalanceChangeHistory cancelHistory(Long historyId, String cancellationReason) {
        Optional<BalanceChangeHistory> historyOpt = balanceChangeHistoryRepository.findById(historyId);
        if (historyOpt.isPresent()) {
            BalanceChangeHistory history = historyOpt.get();

            // Check if the payment is already paid - cannot cancel paid payments
            if (history.getPaymentStatus() == BalanceChangeHistory.PaymentStatus.PAID) {
                throw new RuntimeException("Cannot cancel a payment that is already paid");
            }

            // Check if already cancelled
            if (history.getPaymentStatus() == BalanceChangeHistory.PaymentStatus.CANCELLED) {
                throw new RuntimeException("Payment is already cancelled");
            }

            // Validate cancellation reason
            if (cancellationReason == null || cancellationReason.trim().isEmpty()) {
                throw new RuntimeException("Cancellation reason is required");
            }

            // Revert the balance change
            User user = history.getUser();
            Double currentBalance = (double) user.getBalance();
            Double amountToRevert = history.getAmount();

            // Calculate the reverted balance based on the original change type
            Double revertedBalance = currentBalance;
            switch (history.getChangeType()) {
                case ADD:
                    revertedBalance = currentBalance - amountToRevert;
                    break;
                case SET:
                    revertedBalance = history.getPreviousBalance();
                    break;
            }

            // Update user balance
            user.setBalance(revertedBalance.floatValue());
            userRepository.save(user);

            // Update user balance in user_balance table
            Optional<UserBalance> userBalanceOpt = userBalanceRepository.findByUser(user);
            if (userBalanceOpt.isPresent()) {
                UserBalance userBalance = userBalanceOpt.get();
                userBalance.setBalance(revertedBalance);
                userBalance.setLastUpdated(new Date());
                userBalanceRepository.save(userBalance);
            }

            // Update history with cancellation
            history.setPaymentStatus(BalanceChangeHistory.PaymentStatus.CANCELLED);
            history.setCancellationReason(cancellationReason);
            history.setIsActive(false);

            return balanceChangeHistoryRepository.save(history);
        }
        throw new RuntimeException("Balance change history not found with id: " + historyId);
    }

    public Long getUnpaidChangesCount() {
        return balanceChangeHistoryRepository.countUnpaidChanges();
    }

    public Long getPaidChangesCount() {
        return balanceChangeHistoryRepository.countPaidChanges();
    }

    public Double getUnpaidAmountsSum() {
        return balanceChangeHistoryRepository.sumUnpaidAmounts() != null ?
               balanceChangeHistoryRepository.sumUnpaidAmounts() : 0.0;
    }

    public Double getPaidAmountsSum() {
        return balanceChangeHistoryRepository.sumPaidAmounts() != null ?
               balanceChangeHistoryRepository.sumPaidAmounts() : 0.0;
    }

    /**
     * Get balance changes aggregated by month for the current year
     * Returns a list of cumulative balance changes for each month
     */
    public List<Double> getBalanceChangesByMonth() {
        List<Double> result = new ArrayList<>();
        LocalDate now = LocalDate.now();
        int year = now.getYear();

        for (int month = 1; month <= now.getMonthValue(); month++) {
            LocalDate startOfMonth = LocalDate.of(year, month, 1);
            LocalDate endOfMonth = LocalDate.of(year, month, YearMonth.of(year, month).lengthOfMonth());

            Date startDate = Date.from(startOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(endOfMonth.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

            Double monthlyChange = balanceChangeHistoryRepository.sumBalanceChangesByDateRange(startDate, endDate);
            result.add(monthlyChange != null ? monthlyChange : 0.0);
        }

        // Make it cumulative
        double cumulative = 0.0;
        for (int i = 0; i < result.size(); i++) {
            cumulative += result.get(i);
            result.set(i, cumulative);
        }

        return result;
    }

    /**
     * Get balance changes aggregated by year
     * Returns a list of balance changes for each year
     */
    public List<Double> getBalanceChangesByYear(int startYear, int endYear) {
        List<Double> result = new ArrayList<>();

        for (int year = startYear; year <= endYear; year++) {
            LocalDate startOfYear = LocalDate.of(year, 1, 1);
            LocalDate endOfYear = LocalDate.of(year, 12, 31);

            Date startDate = Date.from(startOfYear.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(endOfYear.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

            Double yearlyChange = balanceChangeHistoryRepository.sumBalanceChangesByDateRange(startDate, endDate);
            result.add(yearlyChange != null ? yearlyChange : 0.0);
        }

        return result;
    }
}

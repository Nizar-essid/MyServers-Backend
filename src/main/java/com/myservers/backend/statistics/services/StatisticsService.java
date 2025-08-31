package com.myservers.backend.statistics.services;

import com.myservers.backend.security.auth.entities.User;
import com.myservers.backend.servers.entities.Server;
import com.myservers.backend.servers.entities.Subscription;
import com.myservers.backend.servers.services.CodesService;
import com.myservers.backend.servers.services.OnDemandRequestService;
import com.myservers.backend.servers.services.ServersService;
import com.myservers.backend.servers.services.SubscriptionService;
import com.myservers.backend.statistics.classes.GetStatisticsResponse;
import com.myservers.backend.statistics.classes.Statistics;
import com.myservers.backend.users.UserService.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Service
public class StatisticsService {
    private final UserService userService;
    private final ServersService serverService;
    private final SubscriptionService subscriptionService;
    private final OnDemandRequestService onDemandRequestService;
    private final CodesService codesService;

    public GetStatisticsResponse getStatistics(String period) {
        List<User> users = userService.getActiveUsers();
        List<Server> servers = serverService.getAllServers();
        List<Subscription> subscriptions = subscriptionService.findAll();
        if (period.equals("month")) {
            List<String> labels = getMonthsList();
            List<Integer> userData = userService.getCumulativeUserCountByMonth();
            List<Double> purchaseData = getCombinedCumulativePurchasesByMonth();
            List<Integer> serverData = codesService.getCumulativeCodesCountByMonth();
            List<Double> balanceData = userService.getBalanceChangesByMonth();
            return GetStatisticsResponse.builder()
                    .data(
                            Statistics.builder()
                                    .labels(labels)
                                    .userData(userData)
                                    .purchaseData(purchaseData)
                                    .serverData(serverData)
                                    .balanceData(balanceData)
                                    .build())
                    .status(200)
                    .build();
        } else if (period.equals("year")) {
            List<String> labels = List.of("2024", "2025");
            List<Integer> userData = userService.getUsersCountPerYear(2024,2025);
            List<Double> purchaseData = getCombinedPurchasesByYear(2024,2025);
            List<Integer> serverData = codesService.getcodesCountPerYear(2024,2025);
            List<Double> balanceData = userService.getBalanceChangesByYear(2024,2025);
            return GetStatisticsResponse.builder()
                    .data(
                            Statistics.builder()
                                    .labels(labels)
                                    .userData(userData)
                                    .purchaseData(purchaseData)
                                    .serverData(serverData)
                                    .balanceData(balanceData)
                                    .build())
                    .status(200)
                    .build();
        }
        return GetStatisticsResponse.builder()
                .status(400)
                .message("Invalid period specified")
                .build();
    }

    /**
     * Get combined cumulative purchases (subscriptions + on-demand) by month
     */
    private List<Double> getCombinedCumulativePurchasesByMonth() {
        List<Double> subscriptionPurchases = subscriptionService.getCumulativeSubscriptionsSumByMonth();
        List<Double> onDemandPurchases = onDemandRequestService.getCumulativeOnDemandPurchasesSumByMonth();

        // Combine the two lists by adding corresponding elements
        List<Double> combinedPurchases = new java.util.ArrayList<>();
        int maxSize = Math.max(subscriptionPurchases.size(), onDemandPurchases.size());

        for (int i = 0; i < maxSize; i++) {
            double subscriptionAmount = i < subscriptionPurchases.size() ? subscriptionPurchases.get(i) : 0.0;
            double onDemandAmount = i < onDemandPurchases.size() ? onDemandPurchases.get(i) : 0.0;
            combinedPurchases.add(subscriptionAmount + onDemandAmount);
        }

        return combinedPurchases;
    }

    /**
     * Get combined purchases (subscriptions + on-demand) by year
     */
    private List<Double> getCombinedPurchasesByYear(int startYear, int endYear) {
        List<Double> subscriptionPurchases = subscriptionService.getPurchasesPerYear(startYear, endYear);
        List<Double> onDemandPurchases = onDemandRequestService.getOnDemandPurchasesSumByYear(startYear, endYear);

        // Combine the two lists by adding corresponding elements
        List<Double> combinedPurchases = new java.util.ArrayList<>();
        int maxSize = Math.max(subscriptionPurchases.size(), onDemandPurchases.size());

        for (int i = 0; i < maxSize; i++) {
            double subscriptionAmount = i < subscriptionPurchases.size() ? subscriptionPurchases.get(i) : 0.0;
            double onDemandAmount = i < onDemandPurchases.size() ? onDemandPurchases.get(i) : 0.0;
            combinedPurchases.add(subscriptionAmount + onDemandAmount);
        }

        return combinedPurchases;
    }

    List<String> getMonthsList(){
        LocalDate now = LocalDate.now();
        var months = List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
        return months.subList(0, now.getMonthValue());
    }
}

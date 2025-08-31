package com.myservers.backend.servers.services;


import com.myservers.backend.exceptions.ApiRequestException;
import com.myservers.backend.notifications.enums.NotificationDisplayType;
import com.myservers.backend.notifications.enums.NotificationTargetType;
import com.myservers.backend.notifications.enums.NotificationType;
import com.myservers.backend.notifications.services.NotificationService;
import com.myservers.backend.security.auth.entities.Role;
import com.myservers.backend.security.auth.entities.User;
import com.myservers.backend.servers.classes.AllServersResponse;
import com.myservers.backend.servers.classes.CodeType;
import com.myservers.backend.servers.classes.GeneralResponse;
import com.myservers.backend.servers.classes.Purchase;
import com.myservers.backend.servers.entities.CodeState;
import com.myservers.backend.servers.entities.Subscription;
import com.myservers.backend.servers.entities.SubscrptionState;
import com.myservers.backend.servers.repositories.CodeRepository;
import com.myservers.backend.servers.repositories.SubscriptionRepository;
import com.myservers.backend.servers.repositories.OnDemandRequestRepository;
import com.myservers.backend.servers.repositories.ServerRepository;
import com.myservers.backend.servers.entities.OnDemandRequest;
import com.myservers.backend.servers.entities.RequestStatus;
import com.myservers.backend.servers.entities.ServerType;
import com.myservers.backend.users.classes.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SubscriptionService {
    @Autowired
    private SubscriptionRepository subscriptionRepository;
  @Autowired
  private CodeRepository codeRepository;
  @Autowired
  private NotificationService notificationService;
  @Autowired
  private OnDemandRequestRepository onDemandRequestRepository;
  @Autowired
  private ServerRepository serverRepository;

    // ajouter etudiant

  public void saveSubscription(Subscription p) {
        subscriptionRepository.save(p);
    }

    public Iterable<Subscription> getSubscription() {
        return subscriptionRepository.findAll();
    }

    // deletetudiant
    public void delete(long id) {
        subscriptionRepository.deleteById(id);
    }

    public boolean isSubscriptionValid(String verificationCode, Long id_user) {
        var subscription = subscriptionRepository.findByVerificationCodeAndPurchaser_IdAndState(verificationCode, Math.toIntExact(id_user), SubscrptionState.INPROCESS)
                .orElseThrow(() -> new ApiRequestException("invalid Subscription", HttpStatus.NOT_ACCEPTABLE));
        return subscription.getIdSubscription() >= 0 && subscription.getState().equals(SubscrptionState.INPROCESS);
    }


    public Optional<Subscription> getSubscription(String verificationCode, Long id_user) {
        return subscriptionRepository.findByVerificationCodeAndPurchaser_IdAndState(verificationCode, Math.toIntExact(id_user), SubscrptionState.INPROCESS);
    }

    public List<Subscription> findByPurchaser_IdAndState(Integer id) {
        return subscriptionRepository.findByPurchaser_IdAndState(id, SubscrptionState.COMPLETED);

    }

    public List<Subscription> findAll() {
        return (List<Subscription>) subscriptionRepository.findAll();

    }


    public List<Integer> getPurchasesCountByMonth() {

        List<Subscription> subscriptions = subscriptionRepository.findByState(SubscrptionState.COMPLETED);
        Map<YearMonth, Long> subscriptionCountPerMonth = subscriptions.stream()
                .collect(Collectors.groupingBy(
                        subscription -> YearMonth.from(subscription.getDateLatestUpdate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()),
                        Collectors.counting()
                ));

        // Ensure all months are included (Jan to Dec of the current year)
        Map<YearMonth, Long> completeSubscriptionsCount = ensureMonths(subscriptionCountPerMonth);

        // Convert to a list of integers (user count per month)
        List<Integer> subscrptionsCounts = new ArrayList<>(12);  // 12 months in a year
        for (Month month : Month.values()) {
            YearMonth yearMonth = YearMonth.of(YearMonth.now().getYear(), month);
            subscrptionsCounts.add(completeSubscriptionsCount.get(yearMonth).intValue());
        }

        return subscrptionsCounts;

    }


    public List<Double> getPurchasesByMonth() {


        List<Subscription> subscriptions = subscriptionRepository.findByState(SubscrptionState.COMPLETED);
        Map<YearMonth, Double> purchasedSumPerMonth = subscriptions.stream()
                .collect(Collectors.groupingBy(
                        subscription -> YearMonth.from(subscription.getDateLatestUpdate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()),
                        Collectors.summingDouble(subscription -> subscription.getRelatedCode().getPrice().floatValue())
                ));

        // Ensure all months are included (Jan to Dec of the current year)
        Map<YearMonth, Double> completePurchasedSum = ensureMonthsDouble(purchasedSumPerMonth);

        // Convert to a list of integers (user count per month)
        List<Double> purchasedSums = new ArrayList<>(12);  // 12 months in a year
        for (Month month : Month.values()) {
            YearMonth yearMonth = YearMonth.of(YearMonth.now().getYear(), month);
            purchasedSums.add(completePurchasedSum.get(yearMonth).doubleValue());
        }

        return purchasedSums;
    }
    public List<Integer> getPurchasesCountPerYear(int startYear, int endYear) {


        List<Subscription> subscriptions = subscriptionRepository.findByState(SubscrptionState.COMPLETED);
        // Group users by year of creation
        Map<Year, Long> subscriptionsCountsPerYear = subscriptions.stream()
                .collect(Collectors.groupingBy(
                        object -> Year.from(object.getDateLatestUpdate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()),
                        Collectors.counting()
                ));

        // Ensure all years are included in the result (from startYear to endYear)
        Map<Year, Long> completesubscriptionsCounts = ensureYears(subscriptionsCountsPerYear, startYear, endYear);

        // Convert to a list of integers (user count per year)
        List<Integer> objectsCounts = new ArrayList<>();
        for (int year = startYear; year <= endYear; year++) {
            objectsCounts.add(completesubscriptionsCounts.get(Year.of(year)).intValue());
        }

        return objectsCounts;
    }
    public List<Double> getPurchasesPerYear(int startYear, int endYear) {


        List<Subscription> subscriptions = subscriptionRepository.findByState(SubscrptionState.COMPLETED);
        // Group users by year of creation
        Map<Year, Double> subscriptionsCountsPerYear = subscriptions.stream()
                .collect(Collectors.groupingBy(
                        object -> Year.from(object.getDateLatestUpdate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()),
                        Collectors.summingDouble(subscription -> subscription.getRelatedCode().getPrice().floatValue())
                ));

        // Ensure all years are included in the result (from startYear to endYear)
        Map<Year, Double> completesubscriptionsCounts = ensureYearsDouble(subscriptionsCountsPerYear, startYear, endYear);

        // Convert to a list of integers (user count per year)
        List<Double> objectsCounts = new ArrayList<>();
        for (int year = startYear; year <= endYear; year++) {
            objectsCounts.add(completesubscriptionsCounts.get(Year.of(year)).doubleValue());
        }

        return objectsCounts;
    }
    private Map<Year, Long> ensureYears(Map<Year, Long> objectsCountPerYear, int startYear, int endYear) {
        // Create a map for each year from startYear to endYear
        Map<Year, Long> result = new TreeMap<>();

        // Loop through the range of years and ensure each one is present in the map
        for (int year = startYear; year <= endYear; year++) {
            result.put(Year.of(year), objectsCountPerYear.getOrDefault(Year.of(year), 0L));
        }

        return result;
    }
    private Map<Year, Double> ensureYearsDouble(Map<Year, Double> objectsCountPerYear, int startYear, int endYear) {
        // Create a map for each year from startYear to endYear
        Map<Year, Double> result = new TreeMap<>();

        // Loop through the range of years and ensure each one is present in the map
        for (int year = startYear; year <= endYear; year++) {
            result.put(Year.of(year), objectsCountPerYear.getOrDefault(Year.of(year), 0.0));
        }

        return result;
    }



private Map<YearMonth, Long> ensureMonths(Map<YearMonth, Long> objectsCountPerMonth) {
    // Create a map for each month of the current year
    Map<YearMonth, Long> result = new TreeMap<>();
    YearMonth currentMonth = YearMonth.now();

    // Loop through all months of the current year
    for (Month month : Month.values()) {
        YearMonth yearMonth = YearMonth.of(currentMonth.getYear(), month);
        result.put(yearMonth, objectsCountPerMonth.getOrDefault(yearMonth, 0L));
    }

    // Accumulate values month by month (make it cumulative)
    long cumulative = 0;
    for (Month month : Month.values()) {
        YearMonth yearMonth = YearMonth.of(currentMonth.getYear(), month);
        cumulative += result.get(yearMonth);
        result.put(yearMonth, cumulative);
    }

    return result;
}
private Map<YearMonth, Double> ensureMonthsDouble(Map<YearMonth, Double> objectsCountPerMonth) {
    // Create a map for each month of the current year
    Map<YearMonth, Double> result = new TreeMap<>();
    YearMonth currentMonth = YearMonth.now();

    // Loop through all months of the current year
    for (Month month : Month.values()) {
        YearMonth yearMonth = YearMonth.of(currentMonth.getYear(), month);
        result.put(yearMonth, objectsCountPerMonth.getOrDefault(yearMonth, 0.0));
    }

    // Accumulate values month by month (make it cumulative)
    double cumulative = 0.0;
    for (Month month : Month.values()) {
        YearMonth yearMonth = YearMonth.of(currentMonth.getYear(), month);
        cumulative += result.get(yearMonth);
        result.put(yearMonth, cumulative);
    }

    return result;
}

    ///////////////////////////////////////////
    public List<Integer> getCumulativeSubscriptionsCountByMonth() {
        List<Integer> result = new ArrayList<>();
        LocalDate now = LocalDate.now();
        int year = now.getYear();

        for (int month = 1; month <= now.getMonthValue(); month++) {
            // Get the last day of the month
            LocalDate endOfMonth = LocalDate.of(year, month, YearMonth.of(year, month).lengthOfMonth());
            Date endDate = Date.from(endOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());

            long count = subscriptionRepository.countByStateAndDateLatestUpdateBefore(SubscrptionState.COMPLETED,endDate);
            result.add((int) count); // safe if total users < Integer.MAX_VALUE
        }

        return result;
    }


    ///////////////////////////////////////////
    public List<Double> getCumulativeSubscriptionsSumByMonth() {
        List<Double> result = new ArrayList<>();
        LocalDate now = LocalDate.now();
        int year = now.getYear();

        for (int month = 1; month <= now.getMonthValue(); month++) {
            // Get the last day of the month
            LocalDate endOfMonth = LocalDate.of(year, month, YearMonth.of(year, month).lengthOfMonth());
            Date endDate = Date.from(endOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());

           List<Subscription> subscriptions = subscriptionRepository.findByStateAndDateLatestUpdateLessThanEqual(SubscrptionState.COMPLETED,endDate);
           double sum = subscriptions.stream()
                    .mapToDouble(subscription -> subscription.getRelatedCode() != null && subscription.getRelatedCode().getPrice() != null
                        ? subscription.getRelatedCode().getPrice().doubleValue() : 0.0)
                    .sum();
            result.add(sum);
        }

        return result;
    }

    /**
     * Obtenir les statistiques des abonnements par serveur avec croissance mensuelle et annuelle
     * Inclut les serveurs avec codes et les serveurs à la demande
     */
    public Map<Long, Map<String, Object>> getSubscriptionStatsByServer() {
        Map<Long, Map<String, Object>> serverStats = new HashMap<>();

        // Obtenir tous les serveurs actifs
        List<com.myservers.backend.servers.entities.Server> allServers = serverRepository.findByState(true);

        // Pour chaque serveur, calculer les statistiques
        for (com.myservers.backend.servers.entities.Server server : allServers) {
            Long serverId = server.getId();
            Map<String, Object> stats = new HashMap<>();

            if (server.getServerType() == ServerType.ONDEMAND) {
                // Statistiques pour les serveurs à la demande
                List<OnDemandRequest> onDemandRequests = onDemandRequestRepository.findByServer_IdAndStatusOrderByRequestDateDesc(serverId, RequestStatus.APPROVED);

                // Compter les demandes approuvées
                long totalRequests = onDemandRequests.size();
                stats.put("totalSubscriptions", totalRequests);

                // Calculer le revenu total
                double totalRevenue = onDemandRequests.stream()
                    .mapToDouble(request -> request.getPriceAfterDiscount() != null ?
                        request.getPriceAfterDiscount().doubleValue() : request.getPrice().doubleValue())
                    .sum();
                stats.put("totalRevenue", totalRevenue);

                // Calculer la croissance mensuelle
                double monthlyGrowth = calculateMonthlyGrowthForOnDemand(onDemandRequests);
                stats.put("monthlyGrowth", monthlyGrowth);

                // Calculer la croissance annuelle
                double yearlyGrowth = calculateYearlyGrowthForOnDemand(onDemandRequests);
                stats.put("yearlyGrowth", yearlyGrowth);

            } else {
                // Statistiques pour les serveurs avec codes
                List<Subscription> subscriptions = subscriptionRepository.findByRelatedCode_OriginServer_IdAndState(serverId, SubscrptionState.COMPLETED);

                // Compter les abonnements
                long totalSubscriptions = subscriptions.size();
                stats.put("totalSubscriptions", totalSubscriptions);

                // Calculer le revenu total
                double totalRevenue = subscriptions.stream()
                    .filter(subscription -> subscription.getRelatedCode() != null &&
                                          subscription.getRelatedCode().getPrice() != null)
                    .mapToDouble(subscription -> subscription.getRelatedCode().getPrice().doubleValue())
                    .sum();
                stats.put("totalRevenue", totalRevenue);

                // Calculer la croissance mensuelle
                double monthlyGrowth = calculateMonthlyGrowth(subscriptions);
                stats.put("monthlyGrowth", monthlyGrowth);

                // Calculer la croissance annuelle
                double yearlyGrowth = calculateYearlyGrowth(subscriptions);
                stats.put("yearlyGrowth", yearlyGrowth);
            }

            serverStats.put(serverId, stats);
        }

        return serverStats;
    }

    /**
     * Calculer la croissance mensuelle pour un serveur
     */
    private double calculateMonthlyGrowth(List<Subscription> subscriptions) {
        LocalDate now = LocalDate.now();
        LocalDate currentMonthStart = now.withDayOfMonth(1);
        LocalDate previousMonthStart = currentMonthStart.minusMonths(1);
        LocalDate previousMonthEnd = currentMonthStart.minusDays(1);

        // Filtrer les abonnements du mois actuel
        double currentMonthRevenue = subscriptions.stream()
            .filter(sub -> {
                LocalDate subscriptionDate = sub.getDateLatestUpdate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
                return !subscriptionDate.isBefore(currentMonthStart);
            })
            .filter(sub -> sub.getRelatedCode() != null && sub.getRelatedCode().getPrice() != null)
            .mapToDouble(sub -> sub.getRelatedCode().getPrice().doubleValue())
            .sum();

        // Filtrer les abonnements du mois précédent
        double previousMonthRevenue = subscriptions.stream()
            .filter(sub -> {
                LocalDate subscriptionDate = sub.getDateLatestUpdate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
                return !subscriptionDate.isBefore(previousMonthStart) &&
                       !subscriptionDate.isAfter(previousMonthEnd);
            })
            .filter(sub -> sub.getRelatedCode() != null && sub.getRelatedCode().getPrice() != null)
            .mapToDouble(sub -> sub.getRelatedCode().getPrice().doubleValue())
            .sum();

        // Calculer le pourcentage de croissance
        if (previousMonthRevenue == 0) {
            return currentMonthRevenue > 0 ? 100.0 : 0.0;
        }

        return ((currentMonthRevenue - previousMonthRevenue) / previousMonthRevenue) * 100.0;
    }

    /**
     * Calculer la croissance annuelle pour un serveur
     */
    private double calculateYearlyGrowth(List<Subscription> subscriptions) {
        LocalDate now = LocalDate.now();
        LocalDate currentYearStart = now.withDayOfYear(1);
        LocalDate previousYearStart = currentYearStart.minusYears(1);
        LocalDate previousYearEnd = currentYearStart.minusDays(1);

        // Filtrer les abonnements de l'année actuelle
        double currentYearRevenue = subscriptions.stream()
            .filter(sub -> {
                LocalDate subscriptionDate = sub.getDateLatestUpdate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
                return !subscriptionDate.isBefore(currentYearStart);
            })
            .filter(sub -> sub.getRelatedCode() != null && sub.getRelatedCode().getPrice() != null)
            .mapToDouble(sub -> sub.getRelatedCode().getPrice().doubleValue())
            .sum();

        // Filtrer les abonnements de l'année précédente
        double previousYearRevenue = subscriptions.stream()
            .filter(sub -> {
                LocalDate subscriptionDate = sub.getDateLatestUpdate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
                return !subscriptionDate.isBefore(previousYearStart) &&
                       !subscriptionDate.isAfter(previousYearEnd);
            })
            .filter(sub -> sub.getRelatedCode() != null && sub.getRelatedCode().getPrice() != null)
            .mapToDouble(sub -> sub.getRelatedCode().getPrice().doubleValue())
            .sum();

        // Calculer le pourcentage de croissance
        if (previousYearRevenue == 0) {
            return currentYearRevenue > 0 ? 100.0 : 0.0;
        }

        return ((currentYearRevenue - previousYearRevenue) / previousYearRevenue) * 100.0;
    }

    /**
     * Calculer la croissance mensuelle pour les serveurs à la demande
     */
    private double calculateMonthlyGrowthForOnDemand(List<OnDemandRequest> onDemandRequests) {
        LocalDate now = LocalDate.now();
        LocalDate currentMonthStart = now.withDayOfMonth(1);
        LocalDate previousMonthStart = currentMonthStart.minusMonths(1);
        LocalDate previousMonthEnd = currentMonthStart.minusDays(1);

        // Filtrer les demandes du mois actuel
        double currentMonthRevenue = onDemandRequests.stream()
            .filter(request -> {
                LocalDate requestDate = request.getRequestDate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
                return !requestDate.isBefore(currentMonthStart);
            })
            .mapToDouble(request -> request.getPriceAfterDiscount() != null ?
                request.getPriceAfterDiscount().doubleValue() : request.getPrice().doubleValue())
            .sum();

        // Filtrer les demandes du mois précédent
        double previousMonthRevenue = onDemandRequests.stream()
            .filter(request -> {
                LocalDate requestDate = request.getRequestDate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
                return !requestDate.isBefore(previousMonthStart) &&
                       !requestDate.isAfter(previousMonthEnd);
            })
            .mapToDouble(request -> request.getPriceAfterDiscount() != null ?
                request.getPriceAfterDiscount().doubleValue() : request.getPrice().doubleValue())
            .sum();

        // Calculer le pourcentage de croissance
        if (previousMonthRevenue == 0) {
            return currentMonthRevenue > 0 ? 100.0 : 0.0;
        }

        return ((currentMonthRevenue - previousMonthRevenue) / previousMonthRevenue) * 100.0;
    }

    /**
     * Calculer la croissance annuelle pour les serveurs à la demande
     */
    private double calculateYearlyGrowthForOnDemand(List<OnDemandRequest> onDemandRequests) {
        LocalDate now = LocalDate.now();
        LocalDate currentYearStart = now.withDayOfYear(1);
        LocalDate previousYearStart = currentYearStart.minusYears(1);
        LocalDate previousYearEnd = currentYearStart.minusDays(1);

        // Filtrer les demandes de l'année actuelle
        double currentYearRevenue = onDemandRequests.stream()
            .filter(request -> {
                LocalDate requestDate = request.getRequestDate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
                return !requestDate.isBefore(currentYearStart);
            })
            .mapToDouble(request -> request.getPriceAfterDiscount() != null ?
                request.getPriceAfterDiscount().doubleValue() : request.getPrice().doubleValue())
            .sum();

        // Filtrer les demandes de l'année précédente
        double previousYearRevenue = onDemandRequests.stream()
            .filter(request -> {
                LocalDate requestDate = request.getRequestDate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
                return !requestDate.isBefore(previousYearStart) &&
                       !requestDate.isAfter(previousYearEnd);
            })
            .mapToDouble(request -> request.getPriceAfterDiscount() != null ?
                request.getPriceAfterDiscount().doubleValue() : request.getPrice().doubleValue())
            .sum();

        // Calculer le pourcentage de croissance
        if (previousYearRevenue == 0) {
            return currentYearRevenue > 0 ? 100.0 : 0.0;
        }

        return ((currentYearRevenue - previousYearRevenue) / previousYearRevenue) * 100.0;
    }

    public GeneralResponse cancelSubscription(Integer purchaseId) {


        var subscription = subscriptionRepository.findById(Long.valueOf(purchaseId))
                .orElseThrow(() -> new ApiRequestException("Subscription not found", HttpStatus.NOT_FOUND));

        if (subscription.getState() == SubscrptionState.CANCELED) {
            return  GeneralResponse.builder()
              .status(404L)
              .trueFalse(false)
              .result("Subscription is already canceled")
              .build();
        }

        if (subscription.getState() == SubscrptionState.COMPLETED) {
          return  GeneralResponse.builder()
            .status(404L)
            .trueFalse(false)
            .result("Completed subscriptions cannot be canceled")
            .build();
        }

        subscription.setState(SubscrptionState.CANCELED);
        subscription.setDateLatestUpdate(new Date());
        subscriptionRepository.save(subscription);
        var relatedCode = subscription.getRelatedCode();
        relatedCode.setState(CodeState.AVAILABLE);
      codeRepository.save(relatedCode);

      String title = "Achat Anneulé";
      String content = String.format("Votre achat du code de serveur %s de duréé %d Mois est anneulé car vous avez dépassé la date limite de confirmation de l'achat.",
              subscription.getRelatedCode().getOriginServer().getName_serv(),
              subscription.getRelatedCode().getSubscriptionDuration());
      String link = "/client/purchases";
      notificationService.createAutomaticNotification(
        title,
        content,
        NotificationType.REQUEST_REJECTED,
        link,
        NotificationDisplayType.DROPDOWN_NOTIFICATION,
        NotificationTargetType.SPECIFIC_USERS,
        List.of(subscription.getPurchaser().getId()),
        null
      );

        return GeneralResponse.builder()
          .status(200L)
          .trueFalse(true)
          .result("Subscription canceled successfully")
          .build();
    }
}

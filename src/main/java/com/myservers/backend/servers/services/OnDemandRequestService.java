package com.myservers.backend.servers.services;

import com.myservers.backend.servers.classes.GeneralResponse;
import com.myservers.backend.servers.classes.OnDemandRequestResponse;
import com.myservers.backend.servers.classes.ProcessOnDemandRequestRequest;
import com.myservers.backend.servers.entities.OnDemandRequest;
import com.myservers.backend.servers.entities.RequestStatus;
import com.myservers.backend.servers.entities.Server;
import com.myservers.backend.servers.entities.ServerType;
import com.myservers.backend.servers.repositories.OnDemandRequestRepository;
import com.myservers.backend.servers.repositories.ServerRepository;
import com.myservers.backend.security.auth.entities.Admin;
import com.myservers.backend.security.auth.entities.User;
import com.myservers.backend.security.auth.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.HashMap;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class OnDemandRequestService {

    @Autowired
    private OnDemandRequestRepository onDemandRequestRepository;

    @Autowired
    private ServerRepository serverRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Créer une nouvelle demande on-demand
     */
    @Transactional
    public GeneralResponse createOnDemandRequest(User user, Long serverId, String macAddress,
                                               String deviceKey, Integer durationMonths, Float priceAfterDiscount) {
        try {
            // Vérifier que le serveur existe et est de type ONDEMAND
            Optional<Server> serverOpt = serverRepository.findById(serverId);
            if (serverOpt.isEmpty()) {
                return GeneralResponse.builder()
                        .status(404L)
                        .result("Server not found")
                        .trueFalse(false)
                        .build();
            }

            Server server = serverOpt.get();
            if (server.getServerType() != ServerType.ONDEMAND) {
                return GeneralResponse.builder()
                        .status(400L)
                        .result("This server does not support on-demand requests")
                        .trueFalse(false)
                        .build();
            }

            // Vérifier si la MAC address est déjà utilisée pour ce serveur
            if (onDemandRequestRepository.existsByServerIdAndMacAddress(serverId, macAddress)) {
                return GeneralResponse.builder()
                        .status(400L)
                        .result("MAC address already in use for this server")
                        .trueFalse(false)
                        .build();
            }

            // Utiliser le prix fixe du serveur comme prix de base
            Float basePrice = server.getPrice() != null ? server.getPrice().floatValue() : null;
            if (basePrice == null || basePrice <= 0) {
                return GeneralResponse.builder()
                        .status(400L)
                        .result("Server price is not configured")
                        .trueFalse(false)
                        .build();
            }

            // Utiliser le prix après remise si fourni, sinon utiliser le prix de base
            Float finalPrice = priceAfterDiscount != null ? priceAfterDiscount : basePrice;

            // Vérifier le solde de l'utilisateur
            if (user.getBalance() < finalPrice) {
                return GeneralResponse.builder()
                        .status(400L)
                        .result("Insufficient balance. Required: " + finalPrice + " DT, Available: " + user.getBalance() + " DT")
                        .trueFalse(false)
                        .build();
            }

            // Créer la demande
            OnDemandRequest request = new OnDemandRequest(user, server, macAddress, deviceKey, basePrice, priceAfterDiscount, durationMonths);
            onDemandRequestRepository.save(request);

            // Déduire le montant du solde de l'utilisateur
            user.setBalance(user.getBalance() - finalPrice);
            userRepository.save(user);

            return GeneralResponse.builder()
                    .status(200L)
                    .result("On-demand request created successfully. Amount deducted: " + finalPrice + " DT")
                    .trueFalse(true)
                    .build();

        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error creating on-demand request: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }

    /**
     * Traiter une demande on-demand (approuver/rejeter)
     */
    @Transactional
    public GeneralResponse processOnDemandRequest(Admin admin, ProcessOnDemandRequestRequest request) {
        try {
            Optional<OnDemandRequest> requestOpt = onDemandRequestRepository.findById(request.getRequestId());
            if (requestOpt.isEmpty()) {
                return GeneralResponse.builder()
                        .status(404L)
                        .result("Request not found")
                        .trueFalse(false)
                        .build();
            }

            OnDemandRequest onDemandRequest = requestOpt.get();

            // Vérifier que la demande est en attente
            if (onDemandRequest.getStatus() != RequestStatus.PENDING) {
                return GeneralResponse.builder()
                        .status(400L)
                        .result("Request is not pending")
                        .trueFalse(false)
                        .build();
            }

            // Mettre à jour le statut
            onDemandRequest.setStatus(request.getStatus());
            onDemandRequest.setProcessedDate(new Date());
            onDemandRequest.setProcessedBy(admin);
            onDemandRequest.setAdminNotes(request.getAdminNotes());

            // Si rejetée, rembourser l'utilisateur
            if (request.getStatus() == RequestStatus.REJECTED) {
                User user = onDemandRequest.getUser();
                Float refundAmount = onDemandRequest.getPriceAfterDiscount() != null ?
                    onDemandRequest.getPriceAfterDiscount() : onDemandRequest.getPrice();
                user.setBalance(user.getBalance() + refundAmount);
                userRepository.save(user);

                return GeneralResponse.builder()
                        .status(200L)
                        .result("Request rejected. Amount refunded: " + refundAmount + " DT")
                        .trueFalse(true)
                        .build();
            }

            onDemandRequestRepository.save(onDemandRequest);

            return GeneralResponse.builder()
                    .status(200L)
                    .result("Request approved successfully")
                    .trueFalse(true)
                    .build();

        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error processing request: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }

    /**
     * Obtenir toutes les demandes d'un utilisateur
     */
    public List<OnDemandRequestResponse> getUserRequests(Long userId) {
        List<OnDemandRequest> requests = onDemandRequestRepository.findByUser_IdOrderByRequestDateDesc(userId);
        return convertToResponseList(requests);
    }

    /**
     * Obtenir toutes les demandes en attente (pour l'admin)
     */
    public List<OnDemandRequestResponse> getPendingRequests() {
        List<OnDemandRequest> requests = onDemandRequestRepository.findByStatusOrderByRequestDateAsc(RequestStatus.PENDING);
        return convertToResponseList(requests);
    }

    /**
     * Obtenir toutes les demandes (pour l'admin)
     */
    public List<OnDemandRequestResponse> getAllRequests() {
        List<OnDemandRequest> requests = onDemandRequestRepository.findAll();
        return convertToResponseList(requests);
    }

    /**
     * Convertir les entités en DTOs
     */
    private List<OnDemandRequestResponse> convertToResponseList(List<OnDemandRequest> requests) {
        List<OnDemandRequestResponse> responses = new ArrayList<>();

        for (OnDemandRequest request : requests) {
            OnDemandRequestResponse response = OnDemandRequestResponse.builder()
                    .id(request.getId())
                    .userId(request.getUser().getId().longValue())
                    .userEmail(request.getUser().getEmail())
                    .userName(request.getUser().getFirstname() + " " + request.getUser().getLastname())
                    .serverId(request.getServer().getId())
                    .serverName(request.getServer().getName_serv())
                    .serverLogo(request.getServer().getLogo())
                    .macAddress(request.getMacAddress())
                    .deviceKey(request.getDeviceKey())
                    .price(request.getPrice())
                    .priceAfterDiscount(request.getPriceAfterDiscount())
                    .durationMonths(request.getDurationMonths())
                    .status(request.getStatus())
                    .requestDate(request.getRequestDate())
                    .processedDate(request.getProcessedDate())
                    .adminNotes(request.getAdminNotes())
                    .processedByEmail(request.getProcessedBy() != null ? request.getProcessedBy().getEmail() : null)
                    .build();

            responses.add(response);
        }

        return responses;
    }

    /**
     * Get cumulative on-demand purchases sum by month for the current year
     * Returns a list of cumulative purchase amounts for each month
     */
    public List<Double> getCumulativeOnDemandPurchasesSumByMonth() {
        List<Double> result = new ArrayList<>();
        LocalDate now = LocalDate.now();
        int year = now.getYear();

        for (int month = 1; month <= now.getMonthValue(); month++) {
            // Get the last day of the month
            LocalDate endOfMonth = LocalDate.of(year, month, YearMonth.of(year, month).lengthOfMonth());
            Date endDate = Date.from(endOfMonth.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

            List<OnDemandRequest> approvedRequests = onDemandRequestRepository.findByStatusAndRequestDateLessThanEqual(RequestStatus.APPROVED, endDate);
            double sum = approvedRequests.stream()
                .mapToDouble(request -> request.getPriceAfterDiscount() != null ?
                    request.getPriceAfterDiscount().doubleValue() : request.getPrice().doubleValue())
                .sum();
            result.add(sum);
        }

        return result;
    }

    /**
     * Get on-demand purchases sum by year
     * Returns a list of purchase amounts for each year
     */
    public List<Double> getOnDemandPurchasesSumByYear(int startYear, int endYear) {
        List<Double> result = new ArrayList<>();

        for (int year = startYear; year <= endYear; year++) {
            LocalDate startOfYear = LocalDate.of(year, 1, 1);
            LocalDate endOfYear = LocalDate.of(year, 12, 31);

            Date startDate = Date.from(startOfYear.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(endOfYear.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

            List<OnDemandRequest> approvedRequests = onDemandRequestRepository.findByStatusAndRequestDateBetween(RequestStatus.APPROVED, startDate, endDate);
            double sum = approvedRequests.stream()
                .mapToDouble(request -> request.getPriceAfterDiscount() != null ?
                    request.getPriceAfterDiscount().doubleValue() : request.getPrice().doubleValue())
                .sum();
            result.add(sum);
        }

        return result;
    }


}

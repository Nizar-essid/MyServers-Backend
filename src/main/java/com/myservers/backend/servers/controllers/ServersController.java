package com.myservers.backend.servers.controllers;

import com.myservers.backend.email.EmailService;
import com.myservers.backend.exceptions.ApiRequestException;
import com.myservers.backend.security.auth.entities.User;
import com.myservers.backend.security.config.JwtService;
import com.myservers.backend.security.encryption_decryption.EncryptionUtil;
import com.myservers.backend.servers.classes.AllServersResponse;
import com.myservers.backend.servers.classes.CodeType;
import com.myservers.backend.servers.classes.GeneralResponse;
import com.myservers.backend.servers.entities.*;
import com.myservers.backend.servers.repositories.CodeRepository;
import com.myservers.backend.servers.repositories.SubscriptionRepository;
import com.myservers.backend.servers.responsesDataType.AllServersRequestResponse;
import com.myservers.backend.servers.services.CodesService;
import com.myservers.backend.servers.services.ServersService;
import com.myservers.backend.servers.services.SubscriptionService;
import com.myservers.backend.servers.services.VerificationCodeService;
import com.myservers.backend.servers.services.OnDemandRequestService;
import com.myservers.backend.notifications.services.NotificationService;
import com.myservers.backend.notifications.enums.NotificationType;
import com.myservers.backend.notifications.enums.NotificationDisplayType;
import com.myservers.backend.notifications.enums.NotificationTargetType;
import com.myservers.backend.security.auth.entities.Role;
import com.myservers.backend.security.auth.repositories.UserRepository;
import com.myservers.backend.servers.classes.OnDemandRequestResponse;
import com.myservers.backend.statistics.classes.GetStatisticsRequest;
import com.myservers.backend.statistics.classes.GetStatisticsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.HashSet;

@CrossOrigin(origins = "*", exposedHeaders = "**")

@RestController
@RequestMapping("/api/v1/servers/basic")
public class ServersController {
    @Autowired
    private ServersService serversService;
    @Autowired
    private JwtService authService;
    @Autowired
    private VerificationCodeService verifCodeService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private Environment env;
    @Autowired
    private CodesService codesService;
    @Autowired
    private VerificationCodeService verificationCodeService;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private CodeRepository codeRepository;
    @Autowired
    private OnDemandRequestService onDemandRequestService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;
//
    @Autowired
    private com.myservers.backend.servers.services.DiscountsService discountsService;

    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @GetMapping("/get_servers")
    public AllServersRequestResponse getAllServers() {
//        System.out.println("get_servers");
        var servers = new ArrayList<AllServersResponse>();
        serversService.getAllServers().forEach(server -> {
            var serverResponse = AllServersResponse.builder()
                    .id(server.getId())
                    .name_serv(server.getName_serv())
                    .logo(server.getLogo())
                    .serverType(server.getServerType() != null ? server.getServerType().toString() : "CODE_BASED")
                    .description(server.getDescription())
                    .price(server.getPrice())
                    .duration_months(server.getDuration_months())
                    .active(server.getActive())
                    .state(server.isState())
                    .build();
            var codes = new ArrayList<CodeType>();

            // Get available codes for this server
            codesService.getCodesByServerID(Math.toIntExact(server.getId())).forEach(code -> {
                codes.add(CodeType.builder()
                        .price(code.getPrice())
                        .duration(code.getSubscriptionDuration())
                        .build());
            });

            // If no codes available, get all codes to show pricing info
            if (codes.isEmpty()) {
                var allCodes = codeRepository.findByOriginServer_Id(server.getId());
                var uniquePricing = new HashSet<String>();

                allCodes.forEach(code -> {
                    String pricingKey = code.getPrice() + "_" + code.getSubscriptionDuration();
                    if (!uniquePricing.contains(pricingKey)) {
                        uniquePricing.add(pricingKey);
                        codes.add(CodeType.builder()
                                .price(code.getPrice())
                                .duration(code.getSubscriptionDuration())
                                .build());
                    }
                });
            }

            serverResponse.setCodes(codes);
            servers.add(serverResponse);

        });
        return AllServersRequestResponse.builder()
                .status(200)
                .message("success")
                .data(servers)
                .build();
    }

    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @PostMapping("/buy_code")
    public GeneralResponse buyCode(@RequestBody Map<String, Object> requestBody) {
        Integer server_id = (Integer) requestBody.get("server_id");
        Integer duration = (Integer) requestBody.get("duration");
        Integer price = (Integer) requestBody.get("price");
        User user = authService.getUser();

        Server server = serversService.getServer(Long.valueOf(server_id)).orElseThrow(
                () -> new ApiRequestException("Server does not exist", HttpStatus.NOT_FOUND)
        );
        var code = codesService.getAvailableCodeBySpecifications(Math.toIntExact(server.getId()), duration, price).orElseThrow(
                () -> new ApiRequestException("No available code Found", HttpStatus.NOT_FOUND)
        );

        // Compute discount server-side as MAX among: default group discount and per-server discounts
        Double priceAfterDiscountComputed = null;
        Double discountPercentageComputed = null;
        try {
            double base = code.getPrice() != null ? code.getPrice().doubleValue() : (price != null ? price.doubleValue() : 0.0);
            double maxDiscount = 0.0;

            var perServerEff = discountsService.computeEffectiveDiscount(user.getId(), server.getId());
            if (perServerEff.isPresent() && perServerEff.get() > 0) {
                maxDiscount = Math.max(maxDiscount, perServerEff.get());
            }
            var defaultGroup = discountsService.computeDefaultGroupDiscount(user.getId());
            if (defaultGroup.isPresent() && defaultGroup.get() > 0) {
                maxDiscount = Math.max(maxDiscount, defaultGroup.get());
            }
            if (maxDiscount > 0) {
                priceAfterDiscountComputed = Math.round(base * (1 - maxDiscount / 100.0) * 100.0) / 100.0;
                discountPercentageComputed = maxDiscount;
            }
        } catch (Exception e) {
            // Ignore discount computation errors, proceed without discount
        }

        Subscription s = Subscription.builder()
                .date_creation(new Date())
                .state(SubscrptionState.INPROCESS)
                .relatedCode(code)
                .dateLatestUpdate(new Date())
                .purchaser(user)
                .verificationCode(verifCodeService.generateToken(code.getId()))
                .priceAfterDiscount(priceAfterDiscountComputed)
                .discountPercentage(discountPercentageComputed)
                .build();
        subscriptionRepository.save(s);
        code.setState(CodeState.REQUESTED);
        codeRepository.save(code);
        try {
            this.sendCodeBuyerVerificationEmail("Nizar Essid", "essid.nizar.123@gmail.com", "Validation d'achat d'un code " + server.getName_serv(), s.getVerificationCode(), server.getName_serv(),code.getSubscriptionDuration()+" mois");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        //System.out.println(code.getId());
        if (code.getId() >= 0)
            return GeneralResponse.builder()
                    .result("Success")
                    .status(200L)
                    .build();

        return GeneralResponse.builder()
                .result("An error has occurred")
                .status(500L)
                .build();


    }

    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @PostMapping("/verifyPurchaseValidity")
    public GeneralResponse verifyPurchaseValidity(@RequestBody Map<String, Object> requestBody) {
        try {
            String verificationCode = (String) requestBody.get("verificationCode");
            User user = authService.getUser();
            if (verificationCodeService.isTokenExpired(verificationCode)) {
                return GeneralResponse.builder()
                        .result("Verification code expired")
                        .status(501L)
                        .trueFalse(false)
                        .build();
            }
            if (!this.subscriptionService.isSubscriptionValid(verificationCode, Long.valueOf(user.getId()))) {
                return GeneralResponse.builder()
                        .result("There is no valid subscription")
                        .trueFalse(false)
                        .status(406L)
                        .build();
            }


            return GeneralResponse.builder()
                    .result("Success")
                    .status(200L)
                    .trueFalse(true)
                    .build();
        } catch (Exception e) {
System.out.println(e.getMessage());

if(e.getMessage().contains("expired"))
    return GeneralResponse.builder()
            .result("Erreur serveur"+e.getMessage())
            .status(501L)
            .trueFalse(false)
            .build();

return GeneralResponse.builder()
                    .result("Erreur serveur"+e.getMessage())
                    .status(500L)
                    .trueFalse(false)
                    .build();
        }


    }



    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @PostMapping("/getCodeShortDetails")
    public GeneralResponse getCodeShortDetails(@RequestBody Map<String, Object> requestBody) {
        try {
            String verificationCode = (String) requestBody.get("verificationCode");
            User user = authService.getUser();
            if (verificationCodeService.isTokenExpired(verificationCode)) {
                return GeneralResponse.builder()
                        .result("Verification code expired")
                        .status(501L)
                        .trueFalse(false)
                        .build();
            }
            var subscription=this.subscriptionService.getSubscription(verificationCode, Long.valueOf(user.getId()))
                    .orElseThrow(()-> new ApiRequestException("invalid Code", HttpStatus.NOT_ACCEPTABLE));


            return GeneralResponse.builder()
                    .result("Success")
                    .status(200L)
                    .trueFalse(true)
                    .singleData(
                            CodeType.builder().server_name(subscription.getRelatedCode().getOriginServer().getName_serv())
                                            .price(subscription.getPriceAfterDiscount() != null ? subscription.getPriceAfterDiscount().floatValue() : subscription.getRelatedCode().getPrice())
                                                    .duration(subscription.getRelatedCode().getSubscriptionDuration())
                                                            .build())
                    .build();
        } catch (Exception e) {
            System.out.println(e.getMessage());

            if(e.getMessage().contains("expired"))
                return GeneralResponse.builder()
                        .result("Erreur serveur"+e.getMessage())
                        .status(501L)
                        .trueFalse(false)
                        .build();

            return GeneralResponse.builder()
                    .result("Erreur serveur"+e.getMessage())
                    .status(500L)
                    .trueFalse(false)
                    .build();
        }


    }

    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @PostMapping("/payServerCodeByAccount")
    public GeneralResponse payServerCodeByAccount(@RequestBody Map<String, Object> requestBody) {
        try {
            String verificationCode = (String) requestBody.get("verificationCode");
            User user = authService.getUser();
            if (verificationCodeService.isTokenExpired(verificationCode)) {
                return GeneralResponse.builder()
                        .result("Verification code expired")
                        .status(501L)
                        .trueFalse(false)
                        .build();
            }

            var subscription=this.subscriptionService.getSubscription(verificationCode, Long.valueOf(user.getId()))
                    .orElseThrow(()-> new ApiRequestException("invalid Code", HttpStatus.NOT_ACCEPTABLE));

double payable = subscription.getPriceAfterDiscount() != null ? subscription.getPriceAfterDiscount() : (subscription.getRelatedCode().getPrice() != null ? subscription.getRelatedCode().getPrice().doubleValue() : 0.0);
if(user.getBalance()<payable)
    return GeneralResponse.builder()
            .result("insufficient balance")
            .status(400L)
            .trueFalse(false)
            .build();

            try {
                // Pay using the previously computed payable amount
                codesService.payCode(user,subscription);

                // Create a user-specific notification about successful purchase and balance deduction
                String title = "Achat confirmé";
                String content = String.format("Votre achat a été confirmé. Un montant de %.2f DT a été déduit de votre solde.", payable);
                String link = "/client/purchases";
                notificationService.createAutomaticNotification(
                        title,
                        content,
                        NotificationType.CODE_PURCHASED,
                        link,
                        NotificationDisplayType.DROPDOWN_NOTIFICATION,
                        NotificationTargetType.SPECIFIC_USERS,
                        List.of(user.getId()),
                        null
                );

                // Notify admins about completed purchase
                List<Integer> adminIds = new ArrayList<>();
                userRepository.findByRoleAndState(Role.ADMIN, true).forEach(admin -> adminIds.add(admin.getId()));
                String adminTitle = "Code acheté";
                String adminContent = String.format("L'utilisateur %s a acheté un code sur le serveur %s (%d mois). Montant: %.2f DT.",
                        user.getEmail(),
                        subscription.getRelatedCode().getOriginServer().getName_serv(),
                        subscription.getRelatedCode().getSubscriptionDuration(),
                        payable);
                notificationService.createAutomaticNotification(
                        adminTitle,
                        adminContent,
                        NotificationType.CODE_PURCHASED,
                        "/admin/purchases",
                        NotificationDisplayType.DROPDOWN_NOTIFICATION,
                        NotificationTargetType.SPECIFIC_USERS,
                        adminIds,
                        null
                );

                return GeneralResponse.builder()
                        .result("Success")
                        .status(200L)
                        .trueFalse(true)
                        .build();
            } catch (RuntimeException e) {
                return GeneralResponse.builder()
                        .result("Erreur serveur"+e.getMessage())
                        .status(500L)
                        .trueFalse(false)
                        .build();            }

        } catch (Exception e) {
            System.out.println(e.getMessage());

            if(e.getMessage().contains("expired"))
                return GeneralResponse.builder()
                        .result("Erreur serveur"+e.getMessage())
                        .status(501L)
                        .trueFalse(false)
                        .build();

            return GeneralResponse.builder()
                    .result("Erreur serveur"+e.getMessage())
                    .status(500L)
                    .trueFalse(false)
                    .build();
        }


    }


    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @GetMapping("/getPurshasedCodes")
    public GeneralResponse getPurshasedCodes() {
        try {
            User user = authService.getUser();
            List<Subscription> subscriptions=subscriptionService.findByPurchaser_IdAndState(user.getId());

            ArrayList<Object> codes = new ArrayList<Object>();
            subscriptions.forEach(s -> {
                CodeType codetype = null;
                try {
                    Float finalPrice = s.getPriceAfterDiscount() != null ? s.getPriceAfterDiscount().floatValue() : s.getRelatedCode().getPrice();
                    Float originalPrice = s.getRelatedCode().getPrice();
                    Float discountPercentage = s.getDiscountPercentage() != null ? s.getDiscountPercentage().floatValue() : null;

                    codetype = CodeType.builder()
                            .code_value(verificationCodeService.generateTokenForCodeValue(generateTwoDigitNumber() + s.getRelatedCode().getCode_value()+ generateTwoDigitNumber()))
                            .price(finalPrice)
                            .originalPrice(originalPrice)
                            .discountPercentage(discountPercentage)
                            .checksum(s.getRelatedCode().getCode_value())
                            .duration(s.getRelatedCode().getSubscriptionDuration())
                            .dateOfPurchase(s.getDateLatestUpdate())
                            .build();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                var server = AllServersResponse.builder()
                        .id(s.getRelatedCode().getOriginServer().getId())
                        .name_serv(s.getRelatedCode().getOriginServer().getName_serv())
                        .logo(s.getRelatedCode().getOriginServer().getLogo())
                        .build();
                codetype.setServer(server);

                    codes.add(codetype);
            });

            return GeneralResponse.builder()
                    .result("success")
                    .status(200L)
                    .data(codes)
                    .build();




        } catch (Exception e) {
            System.out.println(e.getMessage());

            if(e.getMessage().contains("expired"))
                return GeneralResponse.builder()
                        .result("Erreur serveur"+e.getMessage())
                        .status(501L)
                        .trueFalse(false)
                        .build();

            return GeneralResponse.builder()
                    .result("Erreur serveur"+e.getMessage())
                    .status(500L)
                    .trueFalse(false)
                    .build();
        }


    }

    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @PostMapping("/stat")
    public GetStatisticsResponse getStatistics() {
      //  System.out.println("hello from statistics controller");


        return GetStatisticsResponse.builder()
                .status(200)
                .message("test etst tes ").build();
    }


















    public String sendCodeBuyerVerificationEmail(String name, String email, String subject, String verificationCode, String server_name, String duration) {

        String codePurchaiseValidation = env.getProperty("webApplication.codePurchaiseValidation");
        var date = new Date();
        LocalDateTime dateTime = LocalDateTime.now()
                .atZone(ZoneId.of("Europe/Paris")).toLocalDateTime();

        Context context = new Context();
        // Set variables for the template from the POST request data
        context.setVariable("name", name);
        context.setVariable("email", email);
        context.setVariable("verificationCode", verificationCode);
        context.setVariable("subject", subject);
        if (dateTime.getHour() < 12) {
            context.setVariable("greetings", "Bonjour cher(e) ");
        } else {
            context.setVariable("greetings", "Bonsoir cher(e) ");
        }
        context.setVariable("server_name", server_name);
        context.setVariable("duration", duration);
        context.setVariable("lien_activation", codePurchaiseValidation);
        context.setVariable("verification_code", verificationCode);
        try {
            emailService.sendHtmlEmail(email, subject, "CodeSellerVerificationEmailTemplate", context);
            return "Email sent successfully!";
        } catch (Exception e) {
            return "Error sending email: " + e.getMessage();
        }
    }
    public int generateTwoDigitNumber() {
        Random random = new Random();
        return 10 + random.nextInt(90); // Generates a number between 10 and 99
    }

    // ==================== ON-DEMAND ENDPOINTS ====================

    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @PostMapping("/create_on_demand_request")
    public GeneralResponse createOnDemandRequest(@RequestBody Map<String, Object> requestBody) {
        try {
            Long serverId = Long.valueOf(requestBody.get("serverId").toString());
            String macAddress = (String) requestBody.get("macAddress");
            String deviceKey = (String) requestBody.get("deviceKey");
            Integer durationMonths = (Integer) requestBody.get("durationMonths");

            // Extract priceAfterDiscount from request (optional)
            Float priceAfterDiscount = null;
            if (requestBody.containsKey("priceAfterDiscount") && requestBody.get("priceAfterDiscount") != null) {
                priceAfterDiscount = Float.valueOf(requestBody.get("priceAfterDiscount").toString());
            }

            User user = authService.getUser();

            GeneralResponse response = onDemandRequestService.createOnDemandRequest(user, serverId, macAddress, deviceKey, durationMonths, priceAfterDiscount);

            // Notify admins about new on-demand request
            if (response != null && (response.getStatus() == 200L || response.getStatus() == 200)) {
                List<Integer> adminIds = new ArrayList<>();
                userRepository.findByRoleAndState(Role.ADMIN, true).forEach(admin -> adminIds.add(admin.getId()));
                String title = "Nouvelle demande à la demande";
                String content = String.format("L'utilisateur %s a créé une demande pour le serveur %d (%d mois).", user.getEmail(), serverId, durationMonths);
                String link = "/admin/on-demand-requests";
                notificationService.createAutomaticNotification(
                        title,
                        content,
                        NotificationType.SYSTEM_ANNOUNCEMENT,
                        link,
                        NotificationDisplayType.DROPDOWN_NOTIFICATION,
                        NotificationTargetType.SPECIFIC_USERS,
                        adminIds,
                        null
                );
            }

            return response;

        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error creating on-demand request: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }

    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @GetMapping("/get_my_on_demand_requests")
    public GeneralResponse getMyOnDemandRequests() {
        try {
            User user = authService.getUser();
            List<OnDemandRequestResponse> requests = onDemandRequestService.getUserRequests(user.getId().longValue());

            return GeneralResponse.builder()
                    .status(200L)
                    .result("success")
                    .data((ArrayList<Object>) (ArrayList<?>) requests)
                    .trueFalse(true)
                    .build();

        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error getting on-demand requests: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }

    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @PostMapping("/cancel_on_demand_request")
    public GeneralResponse cancelOnDemandRequest(@RequestBody Map<String, Object> requestBody) {
        try {
            Long requestId = Long.valueOf(requestBody.get("requestId").toString());
            String cancellationReason = (String) requestBody.get("cancellationReason");

            User user = authService.getUser();

            GeneralResponse response = onDemandRequestService.cancelOnDemandRequest(requestId, cancellationReason, user);

            // Notify admins about cancelled on-demand request
            if (response != null && (response.getStatus() == 200L || response.getStatus() == 200)) {
                List<Integer> adminIds = new ArrayList<>();
                userRepository.findByRoleAndState(Role.ADMIN, true).forEach(admin -> adminIds.add(admin.getId()));
                String title = "Demande à la demande annulée";
                String content = String.format("L'utilisateur %s a annulé sa demande #%d.", user.getEmail(), requestId);
                String link = "/admin/on-demand-requests";
                notificationService.createAutomaticNotification(
                        title,
                        content,
                        NotificationType.SYSTEM_ANNOUNCEMENT,
                        link,
                        NotificationDisplayType.DROPDOWN_NOTIFICATION,
                        NotificationTargetType.SPECIFIC_USERS,
                        adminIds,
                        null
                );
            }

            return response;

        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error cancelling on-demand request: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }
}

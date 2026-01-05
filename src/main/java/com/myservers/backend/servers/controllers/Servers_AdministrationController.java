package com.myservers.backend.servers.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myservers.backend.exceptions.ApiRequestException;
import com.myservers.backend.notifications.services.NotificationService;
import com.myservers.backend.security.auth.entities.Admin;
import com.myservers.backend.security.auth.entities.User;
import com.myservers.backend.security.config.JwtService;
import com.myservers.backend.security.encryption_decryption.EncryptionUtil;
import com.myservers.backend.servers.classes.*;
import com.myservers.backend.servers.entities.Code;
import com.myservers.backend.servers.classes.UpdateOnDemandServerRequest;
import com.myservers.backend.servers.entities.CodeState;
import com.myservers.backend.servers.entities.Server;
import com.myservers.backend.servers.entities.Subscription;
import com.myservers.backend.servers.responsesDataType.AllServersRequestResponse;
import com.myservers.backend.servers.services.CodesService;
import com.myservers.backend.servers.services.ServersService;
import com.myservers.backend.servers.services.OnDemandRequestService;
import com.myservers.backend.servers.classes.OnDemandRequestResponse;
import com.myservers.backend.servers.classes.ProcessOnDemandRequestRequest;
import com.myservers.backend.servers.entities.RequestStatus;
import com.myservers.backend.security.auth.entities.Admin;
import com.myservers.backend.servers.services.SubscriptionService;
import com.myservers.backend.servers.services.VerificationCodeService;
import com.myservers.backend.statistics.classes.GetStatisticsRequest;
import com.myservers.backend.statistics.classes.GetStatisticsResponse;
import com.myservers.backend.users.classes.UserResponse;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", exposedHeaders = "**")

@RestController
@RequestMapping("/api/v1/servers/admin")
public class Servers_AdministrationController {
    @Autowired
    private ServersService serversService;
    @Autowired
    private CodesService codeService;
    @Autowired
    private OnDemandRequestService onDemandRequestService;
    @Autowired
    private JwtService authService;
    @Autowired
    private Environment env;
    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private NotificationService notificationService;
@Autowired
private VerificationCodeService verificationCodeService;
    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @PostMapping(value="/add_server",consumes = MediaType.APPLICATION_JSON_VALUE)
    public GeneralResponse addServer(@RequestBody AddServerRequest bodyRequest ) throws ParseException {

        try {
            // String logo = json.getServer_details().getLogo();
            User user = authService.getUser();
            var server = Server.builder()
                    .name_serv(bodyRequest.getServer_details().getName_serv())
                    .logo(bodyRequest.getServer_details().getLogo())
                    .added_by((Admin) user)
                    .date_creation(new Date())
                    .state(true)
                    .build();
            server = serversService.saveServer(server);

            Server finalServer = server;
            bodyRequest.getCodes_added().forEach((addCodeDetails code) -> {
                try {
                    codeService.saveCode( Code.builder()
                            .added_by((Admin) user)
                            .originServer(finalServer)
                            .price((float) code.getPrice())
                            .cost(code.getCost() != null ? (float) code.getCost() : null)
                            .subscriptionDuration((int) code.getSubscription_duration())
                            .code_value(EncryptionUtil.encrypt1(code.getValue(), Objects.requireNonNull(env.getProperty("security.AESKEY"))))
                            .state(CodeState.AVAILABLE)
                            .dateCreation(new Date())
                            .build());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            });
            return GeneralResponse.builder()
                    .result(finalServer.getId().toString())
                    .status(200L)
                    .build();
        }catch (Exception e){
            System.out.println(e.getMessage());
            return GeneralResponse.builder()
                    .result("Erreur serveur"+e.getMessage())
                    .status(501L)
                    .trueFalse(false)
                    .build();
        }
    }

    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @PostMapping(value="/create_server",consumes = MediaType.APPLICATION_JSON_VALUE)
    public GeneralResponse createOnDemandServer(@RequestBody CreateOnDemandServerRequest bodyRequest ) throws ParseException {

        try {
            User user = authService.getUser();
            var server = Server.builder()
                    .name_serv(bodyRequest.getName_serv())
                    .description(bodyRequest.getDescription())
                    .price(bodyRequest.getPrice() != null ? bodyRequest.getPrice() : 0.0)
                    .duration_months(bodyRequest.getDuration_months() != null ? bodyRequest.getDuration_months() : 1)
                    .active(bodyRequest.getActive() != null ? bodyRequest.getActive() : true)
                    .logo(bodyRequest.getLogo())
                    .serverType(com.myservers.backend.servers.entities.ServerType.ONDEMAND)
                    .added_by((Admin) user)
                    .date_creation(new Date())
                    .state(true)
                    .build();
            server = serversService.saveServer(server);

            return GeneralResponse.builder()
                    .result(server.getId().toString())
                    .status(200L)
                    .singleData(  server)
                    .build();
        }catch (Exception e){
            System.out.println(e.getMessage());
            return GeneralResponse.builder()
                    .result("Erreur création serveur on-demand: "+e.getMessage())
                    .status(501L)
                    .trueFalse(false)
                    .build();
        }
    }

@CrossOrigin(origins = "*", exposedHeaders = "**")
@PostMapping(value="/add_code",consumes = MediaType.APPLICATION_JSON_VALUE)
public GeneralResponse addCode(@RequestBody AddCodeRequest bodyRequest ) throws ParseException {

    try {
        // String logo = json.getServer_details().getLogo();
        User user = authService.getUser();
        var server = serversService.getServer(bodyRequest.getId_server()).orElseThrow(()-> new ApiRequestException("invalid Subscription", HttpStatus.NOT_ACCEPTABLE));
  var  code=Code.builder()
          .code_value(EncryptionUtil.encrypt1(bodyRequest.getCode_details().getValue(), Objects.requireNonNull(env.getProperty("security.AESKEY"))))
          .state(CodeState.AVAILABLE)
                    .added_by((Admin) user)
                    .originServer(server)
                    .price((float) bodyRequest.getCode_details().getPrice())
                    .cost(bodyRequest.getCode_details().getCost() != null ? (float) bodyRequest.getCode_details().getCost() : null)
                    .subscriptionDuration((int) bodyRequest.getCode_details().getSubscription_duration())
                    .dateCreation(new Date())
                    .build();

        var savedCode=codeService.saveCode(code);
        return GeneralResponse.builder()
                .result(savedCode.getId().toString())
                .status(200L)
                .build();
    }catch (Exception e){
        System.out.println(e.getMessage());
        return GeneralResponse.builder()
                .result("Erreur serveur"+e.getMessage())
                .status(501L)
                .trueFalse(false)
                .build();
    }
}

    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @PostMapping(value="/add_multiple_codes",consumes = MediaType.APPLICATION_JSON_VALUE)
    public GeneralResponse addMultipleCodes(@RequestBody AddMultipleCodesRequest bodyRequest ) throws ParseException {

        try {
            User user = authService.getUser();
            var server = serversService.getServer(bodyRequest.getId_server()).orElseThrow(()-> new ApiRequestException("invalid Server", HttpStatus.NOT_ACCEPTABLE));

            List<Code> savedCodes = new ArrayList<>();

            for (CodeDetails codeDetails : bodyRequest.getCodes()) {
                var code = Code.builder()
                        .code_value(EncryptionUtil.encrypt1(codeDetails.getValue(), Objects.requireNonNull(env.getProperty("security.AESKEY"))))
                        .state(CodeState.AVAILABLE)
                        .added_by((Admin) user)
                        .originServer(server)
                        .price((float) codeDetails.getPrice())
                        .cost(codeDetails.getCost() != null ? (float) codeDetails.getCost() : null)
                        .subscriptionDuration((int) codeDetails.getSubscription_duration())
                        .dateCreation(new Date())
                        .build();

                var savedCode = codeService.saveCode(code);
                savedCodes.add(savedCode);
            }

            return GeneralResponse.builder()
                    .result("Codes ajoutés avec succès: " + savedCodes.size())
                    .status(200L)
                    .build();
        }catch (Exception e){
            System.out.println(e.getMessage());
            return GeneralResponse.builder()
                    .result("Erreur serveur"+e.getMessage())
                    .status(501L)
                    .trueFalse(false)
                    .build();
        }
    }


    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @PostMapping(value="/update_server",consumes = MediaType.APPLICATION_JSON_VALUE)
    public GeneralResponse updateServer(@RequestBody AddServerRequest bodyRequest ) throws ParseException {
        try {
            // String logo = json.getServer_details().getLogo();
            User user = authService.getUser();



            return serversService.saveServer(bodyRequest.getServer_details(),(Admin)user);


        }catch (Exception e){
            System.out.println(e.getMessage());
            return GeneralResponse.builder()
                    .result("Erreur serveur"+e.getMessage())
                    .status(501L)
                    .trueFalse(false)
                    .build();
        }
    }

    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @PostMapping(value="/update_on_demand_server",consumes = MediaType.APPLICATION_JSON_VALUE)
    public GeneralResponse updateOnDemandServer(@RequestBody UpdateOnDemandServerRequest bodyRequest ) throws ParseException {
        try {
            User user = authService.getUser();
            return serversService.updateOnDemandServer(bodyRequest, (Admin)user);
        }catch (Exception e){
            System.out.println(e.getMessage());
            return GeneralResponse.builder()
                    .result("Erreur mise à jour serveur à la demande: "+e.getMessage())
                    .status(501L)
                    .trueFalse(false)
                    .build();
        }
    }
    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @PostMapping(value="/delete_server",consumes = MediaType.APPLICATION_JSON_VALUE)
    public GeneralResponse deleteServer(@RequestBody HashMap<String,Object> bodyRequest ) throws ParseException {
        try {
            return serversService.deleteServer((Integer) bodyRequest.get("id"));


        }catch (Exception e){
            System.out.println(e.getMessage());
            return GeneralResponse.builder()
                    .result("Erreur serveur"+e.getMessage())
                    .status(501L)
                    .trueFalse(false)
                    .build();
        }
    }

    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @PostMapping(value="/update_code",consumes = MediaType.APPLICATION_JSON_VALUE)
    public GeneralResponse updateCode(@RequestBody AddCodeRequest bodyRequest ) throws ParseException {

        try {
            // String logo = json.getServer_details().getLogo();
            User user = authService.getUser();

            return codeService.updateCode(bodyRequest.getCode_details(),user);
        }catch (Exception e){
            System.out.println(e.getMessage());
            return GeneralResponse.builder()
                    .result("Erreur serveur"+e.getMessage())
                    .status(501L)
                    .trueFalse(false)
                    .build();
        }
    }

    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @PostMapping(value="/get_codes_by_server")
    public GeneralResponse get_codes_by_server(@RequestBody HashMap<String,Object> requestBody) throws ParseException {

        try {
            // String logo = json.getServer_details().getLogo();
var id_server=(Integer)requestBody.get("id");
            return GeneralResponse.builder().status(200L)
                    .data( codeService.getCodesResponsesByServerID(id_server))
                            .result("success")
                    .build();
        }catch (Exception e){
            System.out.println(e.getMessage());
            return GeneralResponse.builder()
                    .result("Erreur serveur"+e.getMessage())
                    .status(501L)
                    .trueFalse(false)
                    .build();
        }
    }

    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @PostMapping(value="/get_all_codes_by_server")
    public GeneralResponse get_all_codes_by_server(@RequestBody HashMap<String,Object> requestBody) throws ParseException {

        try {
            var id_server=(Integer)requestBody.get("id");
            return GeneralResponse.builder().status(200L)
                    .data( (ArrayList<Object>) (ArrayList<?>) codeService.getAllCodesResponsesByServerID(id_server))
                            .result("success")
                    .build();
        }catch (Exception e){
            System.out.println(e.getMessage());
            return GeneralResponse.builder()
                    .result("Erreur serveur"+e.getMessage())
                    .status(501L)
                    .trueFalse(false)
                    .build();
        }
    }

    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @PostMapping(value="/get_code_with_value")
    public GeneralResponse get_code_with_value(@RequestBody HashMap<String,Object> requestBody) throws ParseException {

        try {
            // Debug: Log the entire request body
            System.out.println("Request body: " + requestBody);
            System.out.println("Request body keys: " + requestBody.keySet());

            var code_id = requestBody.get("id");
            System.out.println("Extracted code_id: " + code_id + " (type: " + (code_id != null ? code_id.getClass().getSimpleName() : "null") + ")");

            // Check if id is null or invalid
            if (code_id == null) {
                return GeneralResponse.builder()
                        .result("Code ID is required")
                        .status(400L)
                        .trueFalse(false)
                        .build();
            }

            // Convert to Integer safely
            Integer codeId;
            if (code_id instanceof Integer) {
                codeId = (Integer) code_id;
            } else if (code_id instanceof String) {
                try {
                    codeId = Integer.parseInt((String) code_id);
                } catch (NumberFormatException e) {
                    return GeneralResponse.builder()
                            .result("Invalid code ID format")
                            .status(400L)
                            .trueFalse(false)
                            .build();
                }
            } else {
                return GeneralResponse.builder()
                        .result("Invalid code ID type")
                        .status(400L)
                        .trueFalse(false)
                        .build();
            }

            var codeResponse = codeService.getCodeWithValueById(codeId);
            if (codeResponse != null) {
                return GeneralResponse.builder().status(200L)
                        .singleData(codeResponse)
                        .result("success")
                        .build();
            } else {
                return GeneralResponse.builder()
                        .result("Code not found")
                        .status(404L)
                        .trueFalse(false)
                        .build();
            }
        }catch (Exception e){
            System.out.println("Error in get_code_with_value: " + e.getMessage());
            e.printStackTrace();
            return GeneralResponse.builder()
                    .result("Erreur serveur: " + e.getMessage())
                    .status(501L)
                    .trueFalse(false)
                    .build();
        }
    }
    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @PostMapping(value="/delete_code",consumes = MediaType.APPLICATION_JSON_VALUE)
    public GeneralResponse deleteCode(@RequestBody HashMap<String,Object> bodyRequest ) throws ParseException {
        try {
            return codeService.deleteCode((Integer) bodyRequest.get("id"),(Integer) bodyRequest.get("server_id"));


        }catch (Exception e){
            System.out.println(e.getMessage());
            return GeneralResponse.builder()
                    .result("Erreur serveur"+e.getMessage())
                    .status(501L)
                    .trueFalse(false)
                    .build();
        }
    }


    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @GetMapping("/get_servers_for_admin")
    public AllServersRequestResponse getAllServers() {
       // System.out.println("get_servers");
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
                    .build();
            var codes = new ArrayList<CodeType>();

            codeService.getCodesByServerID(Math.toIntExact(server.getId())).forEach(code -> {

                codes.add(CodeType.builder()
                                .id(Math.toIntExact(code.getId()))
                                .code_value(verificationCodeService.generateTokenForCodeValue(generateTwoDigitNumber() +code.getCode_value()+ generateTwoDigitNumber()))
                        .price(code.getPrice())
                        .duration(code.getSubscriptionDuration())
                        .build());
            });
            serverResponse.setCodes(codes);
            servers.add(serverResponse);

        });
        return AllServersRequestResponse.builder()
                .status(200)
                .message("success")
                .data(servers)
                .build();
    }
    public int generateTwoDigitNumber() {
        Random random = new Random();
        return 10 + random.nextInt(90); // Generates a number between 10 and 99
    }



    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @GetMapping("/getAllPurshasedCodes")
    public GeneralResponse getAllPurshasedCodes() {
        try {

            List<Subscription> subscriptions=subscriptionService.findAll();
            ArrayList<Object> purchases = new ArrayList<Object>();

            subscriptions.forEach(s -> {

                CodeType codetype = null;
                try {
                    codetype = CodeType.builder()
                            .code_value(verificationCodeService.generateTokenForCodeValue(generateTwoDigitNumber() + s.getRelatedCode().getCode_value()+ generateTwoDigitNumber()))
                            .price(s.getRelatedCode().getPrice())
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

                purchases.add(Purchase.builder()
                                .id(Math.toIntExact(s.getIdSubscription()))
                                .price_after_discount(s.getPriceAfterDiscount() != null ? s.getPriceAfterDiscount().floatValue() : s.getRelatedCode().getPrice())
                                .state(s.getState())
                                .Code(codetype)
                                .user(UserResponse.builder().email(s.getPurchaser().getEmail()).build())

                        .build());
            });

            return GeneralResponse.builder()
                    .result("success")
                    .status(200L)
                    .data(purchases)
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
    @PostMapping("/get_statistics")
    public GetStatisticsResponse getStatistics(@RequestBody HashMap<String,Object> requestBody) {
 //       System.out.println("hello from statistics controller");
//        var servers = new ArrayList<AllServersResponse>();
//        serversService.getAllServers().forEach(server -> {
//            var serverResponse = AllServersResponse.builder()
//                    .id(server.getId())
//                    .name_serv(server.getName_serv())
//                    .logo(server.getLogo())
//                    .build();
//            var codes = new ArrayList<CodeType>();
//
//            codeService.getCodesByServerID(Math.toIntExact(server.getId())).forEach(code -> {
//
//                codes.add(CodeType.builder()
//                        .id(Math.toIntExact(code.getId()))
//                        .code_value(verificationCodeService.generateTokenForCodeValue(generateTwoDigitNumber() +code.getCode_value()+ generateTwoDigitNumber()))
//                        .price(code.getPrice())
//                        .duration(code.getSubscriptionDuration())
//                        .valid_until(code.getValidUntil())
//                        .build());
//            });
//            serverResponse.setCodes(codes);
//            servers.add(serverResponse);
//
//        });
        return GetStatisticsResponse.builder()
                .status(200)
                .message("success")
                .build();
    }

    // ==================== SERVER STATS ENDPOINTS ====================

    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @GetMapping("/get_server_stats")
    public GeneralResponse getServerStats() {
        try {
            // Get subscription stats by server from SubscriptionService
            Map<Long, Map<String, Object>> serverStats = subscriptionService.getSubscriptionStatsByServer();

            // Convert to the format expected by the frontend
            Map<String, Object> formattedStats = new HashMap<>();

            for (Map.Entry<Long, Map<String, Object>> entry : serverStats.entrySet()) {
                Long serverId = entry.getKey();
                Map<String, Object> stats = entry.getValue();

                Map<String, Object> serverStat = new HashMap<>();
                serverStat.put("totalSubscriptions", stats.get("totalSubscriptions"));
                serverStat.put("totalRevenue", stats.get("totalRevenue"));
                serverStat.put("monthlyGrowth", stats.get("monthlyGrowth"));
                serverStat.put("yearlyGrowth", stats.get("yearlyGrowth"));

                formattedStats.put(serverId.toString(), serverStat);
            }

            return GeneralResponse.builder()
                    .status(200L)
                    .result("success")
                    .data(new ArrayList<>(List.of(formattedStats)))
                    .trueFalse(true)
                    .build();

        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error getting server stats: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }

    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @GetMapping("/get_on_demand_stats")
    public GeneralResponse getOnDemandStats() {
        try {
            // Get all on-demand requests
            List<OnDemandRequestResponse> requests = onDemandRequestService.getAllRequests();

            // Calculate statistics
            long pending = requests.stream()
                    .filter(r -> r.getStatus() == RequestStatus.PENDING)
                    .count();

            long approved = requests.stream()
                    .filter(r -> r.getStatus() == RequestStatus.APPROVED)
                    .count();

            long rejected = requests.stream()
                    .filter(r -> r.getStatus() == RequestStatus.REJECTED)
                    .count();

            // Calculate average response time (in hours)
            double avgResponseTime = 0;
            List<OnDemandRequestResponse> processedRequests = requests.stream()
                    .filter(r -> r.getStatus() != RequestStatus.PENDING && r.getRequestDate() != null && r.getProcessedDate() != null)
                    .collect(Collectors.toList());

            if (!processedRequests.isEmpty()) {
                long totalResponseTimeMs = 0;

                for (OnDemandRequestResponse req : processedRequests) {
                    long requestTime = req.getRequestDate().getTime();
                    long processedTime = req.getProcessedDate().getTime();
                    totalResponseTimeMs += (processedTime - requestTime);
                }

                // Convert to hours
                avgResponseTime = Math.round((totalResponseTimeMs / processedRequests.size()) / (1000.0 * 60 * 60));
            }

            // Create response object
            Map<String, Object> stats = new HashMap<>();
            stats.put("pending", pending);
            stats.put("approved", approved);
            stats.put("rejected", rejected);
            stats.put("avgResponseTime", avgResponseTime);

            return GeneralResponse.builder()
                    .status(200L)
                    .result("success")
                    .data(new ArrayList<>(List.of(stats)))
                    .trueFalse(true)
                    .build();

        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error getting on-demand stats: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }

    // ==================== ON-DEMAND ADMIN ENDPOINTS ====================

    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @GetMapping("/get_all_on_demand_requests")
    public GeneralResponse getAllOnDemandRequests() {
        try {
            List<OnDemandRequestResponse> requests = onDemandRequestService.getAllRequests();

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
    @GetMapping("/get_pending_on_demand_requests")
    public GeneralResponse getPendingOnDemandRequests() {
        try {
            List<OnDemandRequestResponse> requests = onDemandRequestService.getPendingRequests();

            return GeneralResponse.builder()
                    .status(200L)
                    .result("success")
                    .data((ArrayList<Object>) (ArrayList<?>) requests)
                    .trueFalse(true)
                    .build();

        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error getting pending on-demand requests: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }

    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @PostMapping("/process_on_demand_request")
    public GeneralResponse processOnDemandRequest(@RequestBody Map<String, Object> requestBody) {
        try {
            Long requestId = Long.valueOf(requestBody.get("requestId").toString());
            String status = (String) requestBody.get("status");
            String adminNotes = (String) requestBody.get("adminNotes");

            Admin admin = authService.getAdmin();

            ProcessOnDemandRequestRequest processRequest = ProcessOnDemandRequestRequest.builder()
                    .requestId(requestId)
                    .status(RequestStatus.valueOf(status))
                    .adminNotes(adminNotes)
                    .build();

            return onDemandRequestService.processOnDemandRequest(admin, processRequest);

        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error processing on-demand request: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }


  @CrossOrigin(origins = "*", exposedHeaders = "**")
  @PostMapping(value="/cancel_purchase",consumes = MediaType.APPLICATION_JSON_VALUE)
  public GeneralResponse CancelPurchase(@RequestBody HashMap<String,Object> bodyRequest ) throws ParseException {
    try {
      return subscriptionService.cancelSubscription((Integer) bodyRequest.get("purchaseId"));


    }catch (Exception e){
      System.out.println(e.getMessage());
      return GeneralResponse.builder()
        .result("Erreur serveur"+e.getMessage())
        .status(501L)
        .trueFalse(false)
        .build();
    }
  }
}

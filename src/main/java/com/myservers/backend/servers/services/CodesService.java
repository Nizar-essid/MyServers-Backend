package com.myservers.backend.servers.services;


import com.myservers.backend.security.auth.entities.User;
import com.myservers.backend.security.auth.repositories.UserRepository;
import com.myservers.backend.security.encryption_decryption.EncryptionUtil;
import com.myservers.backend.servers.classes.CodeBasicResponse;
import com.myservers.backend.servers.classes.CodeDetails;
import com.myservers.backend.servers.classes.CodeResponse;
import com.myservers.backend.servers.classes.GeneralResponse;
import com.myservers.backend.servers.entities.Code;
import com.myservers.backend.servers.entities.CodeState;
import com.myservers.backend.servers.entities.Subscription;
import com.myservers.backend.servers.entities.SubscrptionState;
import com.myservers.backend.servers.repositories.CodeRepository;
import com.myservers.backend.servers.repositories.ServerRepository;
import com.myservers.backend.servers.repositories.SubscriptionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CodesService {
    @Autowired
    private CodeRepository codeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private ServerRepository serverRepository;
    @Autowired
    private Environment env;
    @Autowired
    private VerificationCodeService verificationCodeService;
    public Code saveCode(Code p)
    {

        return codeRepository.save(p);
    }
    public List<Code> getCode() {
        return codeRepository.findAll();
    }

    public Optional<Code> findByIdAndStateAndValidUntilGreaterThanEqual(Long codeId){
    return codeRepository.findByIdAndStateAndValidUntilGreaterThanEqual( codeId,CodeState.REQUESTED, new Date());

    }
    public List<Code> getCodesByServerID(Integer serverID) {
        //return codeRepository.findByOriginServerId(Long.valueOf(serverID));
        return codeRepository.findByOriginServer_IdAndState(Long.valueOf(serverID),CodeState.AVAILABLE);
    }
    public ArrayList<Object> getCodesResponsesByServerID(Integer serverID) {
        //return codeRepository.findByOriginServerId(Long.valueOf(serverID));
        var codesList=new ArrayList<Object>();
         var codes=codeRepository.findByOriginServer_IdAndState(Long.valueOf(serverID),CodeState.AVAILABLE);
       codes.forEach(code->{
           try {
               codesList.add(CodeResponse.builder()
                               .id(Math.toIntExact(code.getId()))
                               .code_value(verificationCodeService.generateTokenForCodeValue(generateTwoDigitNumber() + EncryptionUtil.encrypt1(code.getCode_value(), Objects.requireNonNull(env.getProperty("security.AESKEY")))+ generateTwoDigitNumber()))
                               .date_creation(code.getDateCreation())
                               .latest_update(code.getLastest_Update())
                               .state(code.getState())
                               .duration(code.getSubscriptionDuration())
                               .valid_until(code.getValidUntil())
                               .price(code.getPrice())
                               .purchased_on(code.getPurchasedOn())
                               .build());
           } catch (Exception e) {
               throw new RuntimeException(e);
           }
       });
         return codesList;
    }

    public ArrayList<CodeBasicResponse> getAllCodesResponsesByServerID(Integer serverID) {
        var codesList=new ArrayList<CodeBasicResponse>();
         var codes=codeRepository.findByOriginServer_Id(Long.valueOf(serverID));
       codes.forEach(code->{
           try {
               System.out.println("Processing code: ID=" + code.getId() + ", Price=" + code.getPrice() + ", Duration=" + code.getSubscriptionDuration());
               codesList.add(CodeBasicResponse.builder()
                               .id(Math.toIntExact(code.getId()))
                               .date_creation(code.getDateCreation())
                               .latest_update(code.getLastest_Update())
                               .state(code.getState())
                               .duration(code.getSubscriptionDuration())
                               .valid_until(code.getValidUntil())
                               .price(code.getPrice())
                               .purchased_on(code.getPurchasedOn())
                               .build());
           } catch (Exception e) {
               System.out.println("Error processing code: " + e.getMessage());
               throw new RuntimeException(e);
           }
       });
         return codesList;
    }

    public CodeResponse getCodeWithValueById(Integer codeId) {
        var code = codeRepository.findById(Long.valueOf(codeId));
        if (code.isPresent()) {
            try {
                return CodeResponse.builder()
                        .id(Math.toIntExact(code.get().getId()))
                        .code_value(verificationCodeService.generateTokenForCodeValue(generateTwoDigitNumber() + code.get().getCode_value()+ generateTwoDigitNumber()))
                        .date_creation(code.get().getDateCreation())
                        .latest_update(code.get().getLastest_Update())
                        .state(code.get().getState())
                        .duration(code.get().getSubscriptionDuration())
                        .valid_until(code.get().getValidUntil())
                        .price(code.get().getPrice())
                        .purchased_on(code.get().getPurchasedOn())
                        .build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }



    // deletetudiant
    public void delete(long id) {
        codeRepository.deleteById(id);
    }

    public Optional<Code> getAvailableCodeBySpecifications(int server_id, Integer duration, Integer price) {
        return Optional.ofNullable(codeRepository.findFirstByOriginServer_IdAndSubscriptionDurationAndPriceOrderByDateCreationAsc(Long.valueOf(server_id), duration, price.floatValue()));
    }



    @Transactional
    public void payCode(User user,  Subscription s) {

        Code code = s.getRelatedCode();
        // Determine payable amount: use discounted price if available, otherwise original code price
        double codePrice = code.getPrice() != null ? code.getPrice().doubleValue() : 0.0;
        double payable = s.getPriceAfterDiscount() != null ? s.getPriceAfterDiscount() : codePrice;

        // Check if the user has sufficient balance
        if (user.getBalance() < payable) {
            throw new RuntimeException("Insufficient balance");
        }

        // Update user balance using the payable amount (cast to float for balance type)
        user.setBalance((float) (user.getBalance() - payable));
        userRepository.save(user);

        // Mark product as purchased
        code.setState(CodeState.PURCHASED);
        codeRepository.save(code);

        s.setState(SubscrptionState.COMPLETED);
        subscriptionRepository.save(s);
    }

    public GeneralResponse updateCode(CodeDetails codeDetails, User user) {

        try{ var code=codeRepository.getReferenceById(Long.valueOf(codeDetails.getId()));
System.out.println("Updating code with ID: " + codeDetails.getId()+ ", Value: " + codeDetails.getValue() + ", Price: " + codeDetails.getPrice() + ", Duration: " + codeDetails.getSubscription_duration() + ", Valid Until: " + codeDetails.getValidUntil());
        System.out.println("Current state: "+ !codeDetails.getValue().isBlank()+"&& "+ (codeDetails.getValue().length()>4));
            if(!codeDetails.getValue().isBlank()&&codeDetails.getValue().length()>4){
              System.out.println("Encrypting code value: " );

              code.setCode_value(EncryptionUtil.encrypt1(codeDetails.getValue(), Objects.requireNonNull(env.getProperty("security.AESKEY"))));}

            code.setState(CodeState.AVAILABLE);
            code.setPrice((float) codeDetails.getPrice());
            code.setSubscriptionDuration((int) codeDetails.getSubscription_duration());
code.setValidUntil((Date)codeDetails.getValidUntil());
            saveCode(code);
            return GeneralResponse.builder()
                    .result("success")
                    .status(200L)
                    .build();
        }
        catch (Exception e){
            e.printStackTrace();
            return GeneralResponse.builder()
                    .status(500L)
                    .result("erreur serveur: " + e.getMessage())
                    .trueFalse(false)
                    .build();}}



    public GeneralResponse deleteCode(Integer id,Integer id_server){
        try{
            var server = serverRepository.getReferenceById(Long.valueOf(id_server));
            //var code=codeRepository.
           if( codeRepository.updateStateByStateNotInAndIdAndOriginServer(CodeState.DELETED, List.of(CodeState.REQUESTED, CodeState.PURCHASED),Long.valueOf(id),server)>0)
            return GeneralResponse.builder()
                    .result("success")
                    .status(200L)
                    .build();
           else
               return GeneralResponse.builder()
                       .result("code not existing or is used")
                       .status(404L)
                       .build();
        }
        catch (Exception e){
            return GeneralResponse.builder()
                    .status(500L)
                    .result("erreur serveur: " + e.getMessage())
                    .trueFalse(false)
                    .build();}
    }

    public List<Code>getCodesREquestedorPurshasedWithinServer(Long id_server){
        return codeRepository.findByOriginServer_IdAndStateIn(id_server,List.of(CodeState.PURCHASED,CodeState.REQUESTED));

    }

    public int generateTwoDigitNumber() {
        Random random = new Random();
        return 10 + random.nextInt(90); // Generates a number between 10 and 99
    }



    public List<Integer> getCodesCountByMonth() {

        List<Code> codes = codeRepository.findByStateNotAndOriginServer_State(CodeState.DELETED, false);
        Map<YearMonth, Long> codesCountPerMonth =codes.stream()
                .collect(Collectors.groupingBy(
                        code -> YearMonth.from(code.getDateCreation().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()),
                        Collectors.counting()
                ));

        // Ensure all months are included (Jan to Dec of the current year)
        Map<YearMonth, Long> completeCodesCount = ensureMonths(codesCountPerMonth);

        // Convert to a list of integers (user count per month)
        List<Integer> codesCounts = new ArrayList<>(12);  // 12 months in a year
        for (Month month : Month.values()) {
            YearMonth yearMonth = YearMonth.of(YearMonth.now().getYear(), month);
            codesCounts.add(completeCodesCount.get(yearMonth).intValue());
        }

        return codesCounts;

    }
    public List<Integer> getcodesCountPerYear(int startYear, int endYear) {


        List<Code> codes = codeRepository.findByStateNotAndOriginServer_State(CodeState.DELETED, false);
        // Group users by year of creation
        Map<Year, Long> codesCountsPerYear = codes.stream()
                .collect(Collectors.groupingBy(
                        object -> Year.from(object.getDateCreation().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()),
                        Collectors.counting()
                ));

        // Ensure all years are included in the result (from startYear to endYear)
        Map<Year, Long> availableCodesCounts = ensureYears(codesCountsPerYear, startYear, endYear);

        // Convert to a list of integers (user count per year)
        List<Integer> objectsCounts = new ArrayList<>();
        for (int year = startYear; year <= endYear; year++) {
            objectsCounts.add(availableCodesCounts.get(Year.of(year)).intValue());
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


    private Map<YearMonth, Long> ensureMonths(Map<YearMonth, Long> objectsCountPerMonth) {
        // Create a map for each month of the current year
        Map<YearMonth, Long> result = new TreeMap<>();
        YearMonth currentMonth = YearMonth.now();

        // Loop through all months of the current year
        for (Month month : Month.values()) {
            YearMonth yearMonth = YearMonth.of(currentMonth.getYear(), month);
            result.put(yearMonth, objectsCountPerMonth.getOrDefault(yearMonth, 0L));
        }
        for( var i=1; i<result.size(); i++){
            if(result.containsKey(YearMonth.of(currentMonth.getYear(), Month.of(i+1)))){
                result.replace(YearMonth.of(currentMonth.getYear(), Month.of(i+1)),result.get(YearMonth.of(currentMonth.getYear(), Month.of(i)))+result.get(YearMonth.of(currentMonth.getYear(), Month.of(i+1))));
            }
        }
        return result;
    }


    ///////////////////////////////////////////
    public List<Integer> getCumulativeCodesCountByMonth() {
        List<Integer> result = new ArrayList<>();
        LocalDate now = LocalDate.now();
        int year = now.getYear();

        for (int month = 1; month <= 12; month++) {
            // Get the last day of the month
            LocalDate endOfMonth = LocalDate.of(year, month, YearMonth.of(year, month).lengthOfMonth());

            // If month is in the future, use current date to avoid counting future codes
            if (month > now.getMonthValue()) {
                endOfMonth = now;
            }

            Date endDate = Date.from(endOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());

            long count = codeRepository.countByStateNotAndOriginServer_StateAndDateCreationLessThanEqual(
                    CodeState.DELETED,
                    true,
                    endDate
            );
            result.add((int) count);
        }

        return result;
    }
}

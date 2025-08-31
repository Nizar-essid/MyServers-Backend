package com.myservers.backend.users.UserService;

import com.myservers.backend.security.auth.entities.Role;
import com.myservers.backend.security.auth.entities.User;
import com.myservers.backend.security.auth.repositories.UserRepository;
import com.myservers.backend.security.auth.tfa.TwoFactorAuthenticationService;
import com.myservers.backend.servers.entities.Subscription;
import com.myservers.backend.servers.entities.SubscrptionState;
import com.myservers.backend.users.classes.GeneralResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;

@RequiredArgsConstructor
@Service
public class UserService {

    @Autowired
    UserRepository userRepository;
    private final TwoFactorAuthenticationService tfaService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private com.myservers.backend.users.services.BalanceChangeHistoryService balanceChangeHistoryService;
    public List<User> getAllUsers(){
        return userRepository.findByState(true);
    }

    public GeneralResponse addUser(String mail, String passwd, int phone, float balances, Role roles, boolean mfaEnabled){

        try{
            User user = User.builder()
                    .email(mail)
                    .password(passwordEncoder.encode(passwd))
                    .telephone(phone)
                    .balance(balances)
                    .role(roles)
                    .mfaEnabled(mfaEnabled)
                    .state(true)
                    .build();

            if(mfaEnabled){
                user.setSecret(tfaService.generateNewSecret());
            }
            userRepository.save(user);

            // Create balance history entry if initial balance is positive
            if (balances > 0) {
                try {
                    // Get current admin (you might need to pass this from the controller)
                    // For now, we'll create a system-generated entry
                    balanceChangeHistoryService.createBalanceChange(
                        user,
                        null, // No specific admin for system-generated entries
                        (double) balances,
                        com.myservers.backend.users.entities.BalanceChangeHistory.ChangeType.SET,
                        com.myservers.backend.users.entities.BalanceChangeHistory.PaymentStatus.PAID,
                        "Initial balance setup"
                    );
                } catch (Exception e) {
                    // Log the error but don't fail the user creation
                    System.err.println("Error creating balance history for new user: " + e.getMessage());
                }
            }

            return GeneralResponse.builder()
                    .status(200L)
                    .result("user added with success")
                    .build();

        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(400L)
                    .result("erreur d'ajout")
                    .build();
        }
    }

    public int deleteUser(int idUser){
        return userRepository.updateStateById(false,idUser);
    }

    public int countAdmin(){
        return userRepository.countByRole("ADMIN");
    }

    public User getUserId(Integer id){
        return userRepository.findById(id).orElse(null);
    }


    public int updateRoleUser(Role role, Integer idUser){
        return userRepository.updateRoleById(role,idUser);
    }
    public int updatePasswd(String password, Integer id){
        return userRepository.updatePasswordById(passwordEncoder.encode(password), id);
    }

    public int updateBalance( float balance,Integer id){
        return userRepository.updateBalanceById( balance, id);
    }

    public int updatePhoneNumber(Integer telephone,Integer id){
        return userRepository.updateTelephoneById(telephone,id);
    }

    public int updateUserState(boolean state, Integer id){
        return userRepository.updateStateById(state, id);
    }
    /*public List<User> searchUser(String searchText){

        return userRepository.findByEmailOrRole(searchText, Role.valueOf(searchText));

    }

    public List<User> searchUsers(String searchtext){
        return userRepository.searchBysearchText(searchtext);
    }*/
    public List<User> searchUser(String searchText){
        return userRepository.searchBysearchTextmail(searchText);
    }

    public List<User> getActiveUsers() {
        List<User> users = userRepository.findByRoleAndState(Role.USER,true);
        return users;
    }

    public List<Integer> getUsersByMonth() {

        List<User> users = userRepository.findByRoleAndState(Role.USER,true);
        Map<YearMonth, Long> usersCountPerMonth = users.stream()
                .collect(Collectors.groupingBy(
                        user -> YearMonth.from(user.getDate_creation().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()),
                        Collectors.counting()
                ));

        // Ensure all months are included (Jan to Dec of the current year)
        Map<YearMonth, Long> completeUsersCount = ensureMonths(usersCountPerMonth);

        // Convert to a list of integers (user count per month)
        List<Integer> userCounts = new ArrayList<>(12);  // 12 months in a year
        for (Month month : Month.values()) {
            YearMonth yearMonth = YearMonth.of(YearMonth.now().getYear(), month);
            userCounts.add(completeUsersCount.get(yearMonth).intValue());
        }

        return userCounts;
    }

    private Map<YearMonth, Long> ensureMonths(Map<YearMonth, Long> usersCountPerMonth) {
        // Create a map for each month of the current year
        Map<YearMonth, Long> result = new TreeMap<>();
        YearMonth currentMonth = YearMonth.now();

        // Loop through all months of the current year
        for (Month month : Month.values()) {
            YearMonth yearMonth = YearMonth.of(currentMonth.getYear(), month);
            result.put(yearMonth, usersCountPerMonth.getOrDefault(yearMonth, 0L));
        }
for( var i=1; i<result.size(); i++){
            if(result.containsKey(YearMonth.of(currentMonth.getYear(), Month.of(i+1)))){
                result.replace(YearMonth.of(currentMonth.getYear(), Month.of(i+1)),result.get(YearMonth.of(currentMonth.getYear(), Month.of(i)))+result.get(YearMonth.of(currentMonth.getYear(), Month.of(i+1))));
            }
        }
        return result;
    }


    /////
    public List<Integer> getUsersCountPerYear(int startYear, int endYear) {
        // Get all users from the database
        List<User> allUsers = userRepository.findByRoleAndState(Role.USER,true);

        // Group users by year of creation
        Map<Year, Long> usersCountPerYear = allUsers.stream()
                .collect(Collectors.groupingBy(
                        user -> Year.from(user.getDate_creation().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()),
                        Collectors.counting()
                ));

        // Ensure all years are included in the result (from startYear to endYear)
        Map<Year, Long> completeUsersCount = ensureYears(usersCountPerYear, startYear, endYear);

        // Convert to a list of integers (user count per year)
        List<Integer> userCounts = new ArrayList<>();
        for (int year = startYear; year <= endYear; year++) {
            userCounts.add(completeUsersCount.get(Year.of(year)).intValue());
        }

        return userCounts;
    }

    private Map<Year, Long> ensureYears(Map<Year, Long> usersCountPerYear, int startYear, int endYear) {
        // Create a map for each year from startYear to endYear
        Map<Year, Long> result = new TreeMap<>();

        // Loop through the range of years and ensure each one is present in the map
        for (int year = startYear; year <= endYear; year++) {
            result.put(Year.of(year), usersCountPerYear.getOrDefault(Year.of(year), 0L));
        }

        return result;
    }


    public List<Double> getBalanceByMonth() {


        List<User> allUsers = userRepository.findByRoleAndState(Role.USER,true);

        // Group users by year of creation
        Map<YearMonth, Double> balanceSumPerMonth = allUsers.stream()
                .collect(Collectors.groupingBy(
                        user -> YearMonth.from(user.getDate_creation().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()),
                        Collectors.summingDouble(user -> user.getBalance())
                ));
        // Ensure all months are included (Jan to Dec of the current year)
        Map<YearMonth, Double> completeBalanceSum = ensureMonthsDouble(balanceSumPerMonth);

        // Convert to a list of integers (user count per month)
        List<Double> balanceSums = new ArrayList<>(12);  // 12 months in a year
        for (Month month : Month.values()) {
            YearMonth yearMonth = YearMonth.of(YearMonth.now().getYear(), month);
            balanceSums.add(completeBalanceSum.get(yearMonth).doubleValue());
        }

        return balanceSums;
    }
    public List<Double> getBalancePerYear(int startYear, int endYear) {
        // Get all users from the database
        List<User> allUsers = userRepository.findByRoleAndState(Role.USER,true);

        // Group users by year of creation
        Map<Year, Double> balanceSumPerYear = allUsers.stream()
                .collect(Collectors.groupingBy(
                        user -> Year.from(user.getDate_creation().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()),
                        Collectors.summingDouble(User::getBalance)
                ));

        // Ensure all years are included in the result (from startYear to endYear)
        Map<Year, Double> completeBalanceCount = ensureYearsDouble(balanceSumPerYear, startYear, endYear);

        // Convert to a list of integers (user count per year)
        List<Double> balanceSum = new ArrayList<>();
        for (int year = startYear; year <= endYear; year++) {
            balanceSum.add(completeBalanceCount.get(Year.of(year)).doubleValue());
        }

        return balanceSum;
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
        for( var i=1; i<result.size(); i++){
            if(result.containsKey(YearMonth.of(currentMonth.getYear(), Month.of(i+1)))){
                result.replace(YearMonth.of(currentMonth.getYear(), Month.of(i+1)),result.get(YearMonth.of(currentMonth.getYear(), Month.of(i)))+result.get(YearMonth.of(currentMonth.getYear(), Month.of(i+1))));
            }
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

///////////////////////////////////////////
public List<Integer> getCumulativeUserCountByMonth() {
    List<Integer> result = new ArrayList<>();
    LocalDate now = LocalDate.now();
    int year = now.getYear();

    for (int month = 1; month <= now.getMonthValue(); month++) {
        // Get the last day of the month
        LocalDate endOfMonth = LocalDate.of(year, month, YearMonth.of(year, month).lengthOfMonth());
        Date endDate = Date.from(endOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());

        long count = userRepository.countUsersCreatedBeforeOrIn(endDate);
        result.add((int) count); // safe if total users < Integer.MAX_VALUE
    }

    return result;
}

    public List<Double> getCumulativeBalanceByMonth() {
        List<Double> result = new ArrayList<>();
        LocalDate now = LocalDate.now();
        int year = now.getYear();

        for (int month = 1; month <= now.getMonthValue(); month++) {
            LocalDate endOfMonth = LocalDate.of(year, month, YearMonth.of(year, month).lengthOfMonth());
            Date endDate = Date.from(endOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());

            double totalBalance = userRepository.getCumulativeBalanceBeforeOrIn(endDate);
            result.add(totalBalance);
        }

        return result;
    }

    public User getUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.orElse(null);
    }

    /**
     * Get balance changes aggregated by month for the current year
     * Returns a list of cumulative balance changes for each month
     */
    public List<Double> getBalanceChangesByMonth() {
        return balanceChangeHistoryService.getBalanceChangesByMonth();
    }

    /**
     * Get balance changes aggregated by year
     * Returns a list of balance changes for each year
     */
    public List<Double> getBalanceChangesByYear(int startYear, int endYear) {
        return balanceChangeHistoryService.getBalanceChangesByYear(startYear, endYear);
    }
}

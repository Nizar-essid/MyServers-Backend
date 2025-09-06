package com.myservers.backend.users.UserService;

import com.myservers.backend.security.auth.entities.Role;
import com.myservers.backend.security.auth.entities.User;
import com.myservers.backend.security.auth.repositories.UserRepository;
import com.myservers.backend.security.auth.tfa.TwoFactorAuthenticationService;
import com.myservers.backend.security.config.JwtService;
import com.myservers.backend.servers.entities.Subscription;
import com.myservers.backend.servers.entities.SubscrptionState;
import com.myservers.backend.users.classes.GeneralResponse;
import com.myservers.backend.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.context.Context;
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
    private JwtService jwtService;

    @Autowired
    private com.myservers.backend.users.services.BalanceChangeHistoryService balanceChangeHistoryService;

    @Autowired
    private EmailService emailService;
    public List<User> getAllUsers(){
        return userRepository.findByState(true);
    }

    public GeneralResponse addUser(String firstname,String lastname,String mail, String passwd, int phone, float balances, Role roles, boolean mfaEnabled){

        try{
            // Generate random password if not provided
            if (passwd == null || passwd.trim().isEmpty()) {
                passwd = generateRandomPassword();
            }
            System.out.println("Generated Password: " + passwd); // For debugging; remove in production

            User user = User.builder()
                    .firstname(firstname)
                    .lastname(lastname)
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

            // Send HTML email to user with created password
            try {
                Context context = new Context();
                context.setVariable("email", mail);
                context.setVariable("password", passwd);
                context.setVariable("name", "User"); // You might want to extract name from email or add name field
                context.setVariable("subject", "Your Account Has Been Created");
                context.setVariable("loginUrl", "https://your-app-url.com/login"); // Replace with actual login URL

                // Set greeting based on time
                java.time.LocalDateTime dateTime = java.time.LocalDateTime.now()
                    .atZone(java.time.ZoneId.of("Europe/Paris")).toLocalDateTime();
                if (dateTime.getHour() < 12) {
                    context.setVariable("greetings", "Bonjour");
                } else {
                    context.setVariable("greetings", "Bonsoir");
                }

                emailService.sendHtmlEmail(mail, "Your Account Has Been Created", "user-account-creation-email", context);
            } catch (Exception e) {
                System.err.println("Error sending email to new user: " + e.getMessage());
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

    /**
     * Generate a random password for new users
     * @return randomly generated password
     */
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        java.util.Random random = new java.util.Random();

        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

    /**
     * Verify if the provided password matches the user's current password
     * @param user the user entity
     * @param rawPassword the raw password to verify
     * @return true if password matches, false otherwise
     */
    public boolean verifyPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    /**
     * Extract user ID from JWT token
     * @param token the JWT token
     * @return user ID if token is valid, null otherwise
     */
    public Integer getUserIdFromToken(String token) {
        try {
            String email = jwtService.extractUserEmail(token);
            User user = userRepository.findByEmail(email).orElse(null);
            return user != null ? user.getId() : null;
        } catch (Exception e) {
            return null;
        }
    }
}

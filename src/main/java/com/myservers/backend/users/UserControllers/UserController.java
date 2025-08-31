package com.myservers.backend.users.UserControllers;


import ch.qos.logback.core.net.SyslogOutputStream;
import com.myservers.backend.security.auth.entities.Role;
import com.myservers.backend.security.auth.entities.User;
import com.myservers.backend.users.UserService.UserService;
import com.myservers.backend.users.classes.GeneralResponse;
import com.myservers.backend.users.classes.UserResponse;
import com.myservers.backend.users.dto.UserDto;
import com.myservers.backend.users.services.DtoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users/basic")

@Service
public class UserController {

    @Autowired
    UserService userService;
  @Autowired
  private DtoService dtoService;
    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @PostMapping("/get_users")
    public GeneralResponse getUsers(@RequestBody Map<String,Object> RequestBodey){
        try{

            var users = userService.getAllUsers();
            List<UserResponse> allUsers = new ArrayList<UserResponse>();

            users.forEach((usr)-> allUsers.add(UserResponse.builder()
                                    .id(usr.getId())
                                    .firstname(usr.getFirstname())
                                    .lastname(usr.getLastname())
                                    .state(usr.isState())
                                    .role(usr.getRole())
                                    .balance(usr.getBalance())
                                    .email(usr.getEmail())
                                    .telephone(usr.getTelephone())
                                    .build()
                    )
            );
       //     System.out.println(allUsers);


            return GeneralResponse.builder()
                    .status(200L)
                    .result("users Get with success")
                    .dataUsers(allUsers)
                    .build();

        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(400L)
                    .result("Erro got")
                    .build();
        }
    }

    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @PostMapping(value="/add_users", consumes = MediaType.APPLICATION_JSON_VALUE)

    public GeneralResponse addUser(@RequestBody Map<String,Object> RequestBodey){
        try{
            //boolean mfaEnabled=(boolean) RequestBodey.get("mfaEnabled");
            String email = (String) RequestBodey.get("email");
            String password = (String) RequestBodey.get("password");
            Integer telephone = null;
            if (RequestBodey.get("telephone") instanceof Integer) {
                telephone = (Integer) RequestBodey.get("telephone");
            } else if (RequestBodey.get("telephone") instanceof String) {
                telephone = Integer.parseInt((String) RequestBodey.get("telephone"));
            }
            Float balance = ((Number) RequestBodey.get("balance")).floatValue();

            Role role = null;
            if (RequestBodey.get("role") instanceof String) {
                role = Role.valueOf((String) RequestBodey.get("role"));
            } else if (RequestBodey.get("role") instanceof Integer) {
                role = Role.values()[(Integer) RequestBodey.get("role")];
            }

            //System.out.println("user added with success->"+userService.addUser(email,password,telephone,balance,role));

            return userService.addUser(email,password,telephone,balance,role,false);

        } catch (Exception e) {
            System.out.println("erreur d'ajout");
            return GeneralResponse.builder()
                    .status(400L)
                    .result("erreur d'ajout")
                    .build();
        }
    }

    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @PostMapping("/delete_users/{id}")
    public GeneralResponse deleteUser(@PathVariable("id") Integer userId){
        try{
            User user = userService.getUserId(userId);
            if(user==null){
                return GeneralResponse.builder()
                        .status(401L)
                        .result("parametre is required")
                        .build();
            }
            if ("ADMIN".equals(user.getRole())) {
                int adminCount = userService.countAdmin();
                if (adminCount <= 1) {
                    return GeneralResponse.builder()
                            .status(403L)
                            .result("Impossible de supprimer le dernier administrateur")
                            .build();
                }
            }
            int userDeleted =userService.deleteUser(userId);
            if(userDeleted>0){
                return GeneralResponse.builder()
                        .status(200L)
                        .result("user deleted successfully")
                        .build();
            }
            else{
                return GeneralResponse.builder()
                        .status(405L)
                        .result("user id not found")
                        .build();
            }

        } catch (Exception e) {
            e.fillInStackTrace();
            return GeneralResponse.builder()
                    .status(400L)
                    .result("erreur de supprission")
                    .build();
        }
    }

    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @PostMapping("/upadteRole_users/{id}")

    public GeneralResponse upadateUserRole(@RequestBody Map<String, Object> requestBody){
        try{
            Integer userId = (Integer) requestBody.get("id");
            Role role = Role.valueOf((String) requestBody.get("role"));
            int rowsUpdated = userService.updateRoleUser(role,userId);
            if (rowsUpdated > 0) {
                return GeneralResponse.builder()
                        .status(200L)
                        .result("Rôle de l'utilisateur mis à jour avec succès")
                        .build();
            } else {
                return GeneralResponse.builder()
                        .status(404L)
                        .result("Utilisateur introuvable")
                        .build();
            }

        } catch (Exception e) {
            e.fillInStackTrace();
            return GeneralResponse.builder()
                    .status(400L)
                    .result("erreur de modification de role")
                    .build();
        }
    }

/////////////upadtePasswd_users///////////
    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @PostMapping("/upadtePasswd_users")

    public GeneralResponse upadatePassword(@RequestBody Map<String, Object> requestBody){
        try{

            Integer idUser =(Integer) requestBody.get("id");
            String newPassword = (String)requestBody.get("password");
            if(idUser==null || newPassword == null || newPassword.trim().isEmpty() ){
                return GeneralResponse.builder()
                        .status(401L)
                        .result("il y a des donnees null")
                        .build();
            }

            User user =userService.getUserId(idUser);
            if(user==null){
                return GeneralResponse.builder()
                        .status(400L)
                        .result("user not found")
                        .build();
            }
            int userUpdated = userService.updatePasswd(newPassword,idUser);
            if (userUpdated > 0) {
                return GeneralResponse.builder()
                        .status(200L)
                        .result("user update successfully")
                        .build();
            } else {
                return GeneralResponse.builder()
                        .status(202L)
                        .result("user update successfully")
                        .build();
            }


        } catch (Exception e) {
            e.fillInStackTrace();
            return GeneralResponse.builder()
                    .status(400L)
                    .result("erreur de modification")
                    .build();
        }
    }

    /////////////upadtePasswd_users///////////
    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @PostMapping("/upadteBalance_users")

    public GeneralResponse upadateBalance(@RequestBody Map<String, Object> requestBody){
        try{

            Integer idUser =(Integer) requestBody.get("id");
            Float newBalance = ((Number) requestBody.get("balance")).floatValue();
            if(idUser==null  || newBalance==null){
                return GeneralResponse.builder()
                        .status(401L)
                        .result("il y a des donnees null")
                        .build();
            }

            User user =userService.getUserId(idUser);
            if(user==null){
                return GeneralResponse.builder()
                        .status(400L)
                        .result("user not found")
                        .build();
            }
            int userUpdated = userService.updateBalance(newBalance,idUser);
            if (userUpdated > 0) {
                return GeneralResponse.builder()
                        .status(200L)
                        .result("user update successfully")
                        .build();
            } else {
                return GeneralResponse.builder()
                        .status(202L)
                        .result("user update successfully")
                        .build();
            }


        } catch (Exception e) {
            e.fillInStackTrace();
            return GeneralResponse.builder()
                    .status(400L)
                    .result("erreur de modification")
                    .build();
        }
    }


    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @PostMapping("/toggle_status_users/{id}")
    public GeneralResponse toggleUserStatus(@PathVariable("id") Integer userId) {
        try {
            User user = userService.getUserId(userId);
            if (user == null) {
                return GeneralResponse.builder()
                        .status(404L)
                        .result("User not found")
                        .build();
            }

            // Toggle the current state
            boolean newState = !user.isState();
            int rowsUpdated = userService.updateUserState(newState, userId);

            if (rowsUpdated > 0) {
                String statusMessage = newState ? "User activated successfully" : "User deactivated successfully";
                return GeneralResponse.builder()
                        .status(200L)
                        .result(statusMessage)
                        .build();
            } else {
                return GeneralResponse.builder()
                        .status(400L)
                        .result("Failed to update user status")
                        .build();
            }

        } catch (Exception e) {
            e.fillInStackTrace();
            return GeneralResponse.builder()
                    .status(400L)
                    .result("Error updating user status: " + e.getMessage())
                    .build();
        }
    }

    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @PostMapping("/updatePhone_users")
    public GeneralResponse updatePhoneNumber(@RequestBody Map<String, Object> requestBody) {
        try {
            // Récupération des paramètres
            Integer idUser = (Integer) requestBody.get("id");
            String telephoneStr = requestBody.get("telephone").toString();

            // Validation des paramètres
            if ( telephoneStr == null || telephoneStr.isEmpty()) {
                System.out.println("manque de parametre pour la modification de telephone-> "+telephoneStr+",id"+idUser);
                return GeneralResponse.builder()
                        .status(401L)
                        .result("Manque de paramètre")
                        .build();
            }

            // Vérification du format du numéro de téléphone
            if (!telephoneStr.matches("^[2345789]\\d{7}$")) {
                return GeneralResponse.builder()
                        .status(403L)
                        .result("Le numéro de téléphone est invalide")
                        .build();
            }

            // Conversion en Integer
            Integer telephone = Integer.parseInt(telephoneStr);

            // Vérification de l'existence de l'utilisateur
            User user = this.userService.getUserId(idUser);
            if (user == null) {
                return GeneralResponse.builder()
                        .status(402L)
                        .result("Aucun utilisateur trouvé")
                        .build();
            }

            // Mise à jour du numéro de téléphone
            int userPhoneUpdated = this.userService.updatePhoneNumber(telephone, idUser);
            if (userPhoneUpdated > 0) {
                return GeneralResponse.builder()
                        .status(200L)
                        .result("Succès de la modification")
                        .build();
            } else {
                return GeneralResponse.builder()
                        .status(201L)
                        .result("Rien à modifier")
                        .build();
            }

        } catch (NumberFormatException e) {
            return GeneralResponse.builder()
                    .status(403L)
                    .result("Le numéro de téléphone est invalide")
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return GeneralResponse.builder()
                    .status(400L)
                    .result("Erreur lors de la mise à jour du numéro de téléphone")
                    .build();
        }
    }

    @CrossOrigin(origins = "*", exposedHeaders = "**")
    @PostMapping("/search_users")
    public GeneralResponse searchUser(@RequestBody Map<String, Object> requestBody) {
        try {
            String searchText = (String) requestBody.get("searches");
           // System.out.println("text a chercher"+searchText);
            if (searchText == null || searchText.trim().isEmpty()) {
           //     System.out.println("text a chercher"+searchText);
                return GeneralResponse.builder()
                        .status(400L)
                        .result("Le texte de recherche est requis.")
                        .dataUsers(new ArrayList<>()) // Renvoi une liste vide
                        .build();
            }
            List<User> usersfound = userService.searchUser(searchText);
            List<UserResponse> usersFoundedToSend = new ArrayList<>();
            usersfound.forEach(u -> usersFoundedToSend.add(UserResponse.builder()
                    .id(u.getId())
                    .role(u.getRole())
                    .email(u.getEmail())
                    .build()));
           // System.out.println("resultat de recherche"+usersFoundedToSend.toString());
            return GeneralResponse.builder()
                    .status(200L)
                    .result("Users found")
                    .dataUsers(usersFoundedToSend) // Passe correctement les utilisateurs
                    .build();
        } catch (Exception e) {
            System.out.println("L'erreur trouvee ->"+e.getMessage());
            return GeneralResponse.builder()
                    .status(400L)
                    .result("Erreur de recherche")
                    .dataUsers(new ArrayList<>()) // Toujours renvoyer une liste vide en cas d'erreur
                    .build();
        }
    }

  /**
   * Get all users for user management
   */
  @GetMapping("/get_users")
  public ResponseEntity<?> getAllUsers() {
    System.out.println("Fetching all users for user management");
    try {
      List<User> users = userService.getAllUsers();
      List<UserDto> userDtos = dtoService.convertToUserDtoList(users);

      return ResponseEntity.ok(Map.of(
        "status", 200,
        "result", "success",
        "data", userDtos
      ));
    } catch (Exception e) {
      System.err.println("Error fetching all users: " + e.getMessage());
      return ResponseEntity.badRequest().body(Map.of(
        "status", 400,
        "result", "Error: " + e.getMessage()
      ));
    }
  }
}

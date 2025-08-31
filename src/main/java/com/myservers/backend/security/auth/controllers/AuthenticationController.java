package com.myservers.backend.security.auth.controllers;


import com.myservers.backend.security.auth.dataTypes.*;
import com.myservers.backend.security.auth.entities.User;
import com.myservers.backend.security.auth.services.AuthenticationService;
import com.myservers.backend.security.config.JwtService;
import com.myservers.backend.users.classes.GeneralResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*" , exposedHeaders = "**")

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
    private final AuthenticationService service;
    @Autowired
    private JwtService authService;

    public AuthenticationController(AuthenticationService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request){
return ResponseEntity.ok(service.register(request));

    }



    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest request){
        return ResponseEntity.ok(service.loginAdmin(request));

    }
    @PostMapping("/login_admin")
    public ResponseEntity<AuthenticationResponse> loginAdmin(@RequestBody AuthenticationRequest request){
        return ResponseEntity.ok(service.loginAdmin(request));

    }


    @PostMapping("/login_mfa")
    public ResponseEntity<AuthenticationResponse> loginWithMfa(@RequestBody AuthenticationRequest request){
        return ResponseEntity.ok(service.loginAdminWith2FA(request));

    }

    @PostMapping("/verify_mfa")
    public ResponseEntity<?> verifyMfa(@RequestBody VerificationRequest request){
        return ResponseEntity.ok(service.verifyMfa(request));

    }

    @GetMapping("/get_2fa_qr_code")
    public AuthenticationResponse get_2fa_qr_code() {

        User user = authService.getUser();


        return service.GenerateMfaQr(user);
    }
    @PostMapping("/register_mfa")
    public ResponseEntity<?> register_mfa(@RequestBody VerificationRequest request){
        User user = authService.getUser();
        //System.out.println("code: " + request.getCode());
request.setEmail(user.getEmail());
        return ResponseEntity.ok(service.verifyMfa(request));

    }

    @GetMapping("/isMfaEnabled")
    public AuthenticationResponse isMfaEnabled() {
        User user = authService.getUser();

        return AuthenticationResponse.builder()
                .isMfaEnabled(user.isMfaEnabled())
                .build();
    }

    @PostMapping("/disable_mfa")
    public ResponseEntity<?> disable_mfa(@RequestBody VerificationRequest request){
        User user = authService.getUser();
        request.setEmail(user.getEmail());
        //System.out.println("code: " + request.getCode());
    return ResponseEntity.ok(service.disableMfa(request));}


    /*  reset_password
  changePassword
  verify_mfa_password_change*/


    @PostMapping("/reset_password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(@RequestBody ResetPasswordRequest request){
        return ResponseEntity.ok(service.resetPassword(request));

    }

    @PostMapping("/changePassword")
    public ResponseEntity<ResetPasswordResponse> changePassword(@RequestBody ResetPasswordRequest request){
        return ResponseEntity.ok(service.changePassword(request));

    }
    @PostMapping("/verify_mfa_password_change")
    public ResponseEntity<?> verifyMfaPasswordChange(@RequestBody VerificationRequest request){
        return ResponseEntity.ok(service.verifyMfaPasswordChange(request));

    }
}



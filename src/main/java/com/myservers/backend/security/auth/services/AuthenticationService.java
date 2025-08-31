package com.myservers.backend.security.auth.services;


import com.myservers.backend.email.EmailService;
import com.myservers.backend.exceptions.ApiRequestException;
import com.myservers.backend.security.auth.dataTypes.*;
import com.myservers.backend.security.auth.entities.Role;
import com.myservers.backend.security.auth.tfa.TwoFactorAuthenticationService;
import com.myservers.backend.security.config.JwtService;
import com.myservers.backend.security.auth.entities.Admin;
import com.myservers.backend.security.auth.entities.User;
import com.myservers.backend.security.auth.repositories.AdminRepository;
import com.myservers.backend.security.auth.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;
private final  PasswordEncoder passwordEncoder;
private final JwtService jwtService;
private final AuthenticationManager authenticationManager;
    private final AdminRepository adminRepository;
private final TwoFactorAuthenticationService tfaService;
    private final EmailService emailService;

    public AuthenticationResponse register(RegisterRequest request) {
       var user= User.builder()
               .firstname(request.getFirstname())
               .lastname(request.getLastName())
               .email(request.getEmail())
               .password(passwordEncoder.encode(request.getPassword()))
               .role(Role.USER)
               .mfaEnabled(request.isMfaEnabled())
               .build();
repository.save(user);
        var  admin=  new Admin();
                admin.setFirstname((request.getFirstname()));
                  admin.setLastname(request.getLastName());
                          admin.setEmail(request.getEmail());
                                  admin.setPassword(passwordEncoder.encode(request.getPassword()));
                                          admin.setRole(Role.ADMIN);


        adminRepository.save(admin);
var jwtToken=jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();

    }

    public AuthenticationResponse loginUser(AuthenticationRequest request) {
//        System.out.println("Login as user");
//        System.out.println(request.getEmail());
//        System.out.println(request.getPassword());

        var user=repository.findByEmail(request.getEmail())
                .orElseThrow(()-> new ApiRequestException("user not exist",HttpStatus.NOT_FOUND));
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        authenticationManager.authenticate((
                new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword(), authorities)
                ));
//        System.out.println("after authenticationProvider");

        var jwtToken=jwtService.generateToken(user);
//        System.out.println(jwtToken);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .status(200)
                .message("success")
                .secretImageUri(tfaService.generateQrCodeImageUri(user.getSecret()))
                .isMfaEnabled(user.isMfaEnabled())
                .build();
    }


    public AuthenticationResponse loginAdmin(AuthenticationRequest request) {
//        System.out.println("Login");
        var user=adminRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new ApiRequestException("admin does not exist",HttpStatus.NOT_FOUND));
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        authenticationManager.authenticate((
                new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword(), authorities)
        ));

        var jwtToken=jwtService.generateToken(user);
//        System.out.println(jwtToken);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .secretImageUri(tfaService.generateQrCodeImageUri(user.getSecret()))
                .isMfaEnabled(user.isMfaEnabled())
                .build();
    }
    public AuthenticationResponse loginAdminWith2FA(AuthenticationRequest request) {
//        System.out.println("Login");
        var user=adminRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new ApiRequestException("admin does not exist",HttpStatus.NOT_FOUND));
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        authenticationManager.authenticate((
                new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword(), authorities)
        ));
if(user.isMfaEnabled()){
    return AuthenticationResponse.builder()
            .token("")
            .status(200)
            .isMfaEnabled(true)
            .build();
}

        var jwtToken=jwtService.generateToken(user);
//        System.out.println(jwtToken);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .secretImageUri(tfaService.generateQrCodeImageUri(user.getSecret()))
                .isMfaEnabled(false)
                .status(200)
                .build();
    }

    public AuthenticationResponse verifyMfa(VerificationRequest request) {

        User user = (User) repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiRequestException("user not exist", HttpStatus.NOT_FOUND));
        if(tfaService.isOtpNotValid(user.getSecret(),request.getCode())){
            return AuthenticationResponse.builder()
                    .status(401)
                    .message("invalid code")
                    .build();
        }
        var jwtToken=jwtService.generateToken(user);
//        System.out.println(jwtToken);
        if(!user.isMfaEnabled()){
            user.setMfaEnabled(true);
            repository.save(user);
        }

        return AuthenticationResponse.builder()
                .status(200)
                .token(jwtToken)
                .isMfaEnabled(user.isMfaEnabled())
                .build();
    }

    public AuthenticationResponse GenerateMfaQr(User user) {
        if(user.getSecret()==null||user.getSecret().isEmpty() ||!user.isMfaEnabled()){
        user.setSecret(tfaService.generateNewSecret());
        repository.save(user);}
        return AuthenticationResponse.builder()
                .secretImageUri(tfaService.generateQrCodeImageUri(user.getSecret()))
                .isMfaEnabled(user.isMfaEnabled())
                .build();
    }

    public Object disableMfa(VerificationRequest request) {
        User user = (User) repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiRequestException("user not exist", HttpStatus.NOT_FOUND));
        if(tfaService.isOtpNotValid(user.getSecret(),request.getCode())){
            return AuthenticationResponse.builder()
                    .status(401)
                    .message("invalid code")
                    .build();
        }
        user.setMfaEnabled(false);
        repository.save(user);
        return AuthenticationResponse.builder()
                .status(200)
                .message("mfa disabled")
                .build();
    }

    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
        if (request.getEmail().isEmpty()) {
            throw new ApiRequestException("Email cannot be empty", HttpStatus.BAD_REQUEST);
        }
        User user = (User) repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiRequestException("user not exist", HttpStatus.NOT_FOUND));


        String token = jwtService.generatePasswordResetToken(user);
        user.setPasswordResetToken(token);
        repository.save(user);
        if(user.isMfaEnabled()){

            return ResetPasswordResponse.builder()
                .status(200)
                .message("Password reset token generated successfully")
                .rightAccount(true)
                    .mfaEnabled(user.isMfaEnabled())
                .build();}
        else{
            // Send the token to the user's email (implementation not shown)
            emailService.sendPasswordResetEmail(user.getEmail(), token,user.getFirstname());
            return ResetPasswordResponse.builder()
                .status(200)
                .message("veuillez consulter votre email pour le lien de réinitialisation")
                .rightAccount(true)
                    .mfaEnabled(user.isMfaEnabled())

                    .build();}
    }

    public ResetPasswordResponse changePassword(ResetPasswordRequest request) {
        User user = (User) repository.findByPasswordResetToken(request.getSecret()).orElseThrow(() -> new ApiRequestException("Invalid token", HttpStatus.BAD_REQUEST));
        if (request.getPassword().isEmpty()) {
            throw new ApiRequestException("Password cannot be empty", HttpStatus.BAD_REQUEST);}

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPasswordResetToken(null);
        repository.save(user);
        return ResetPasswordResponse.builder()
                .status(200)
                .message("Password changed successfully")
                .mfaEnabled(user.isMfaEnabled())

                .build();
    }

    public Object verifyMfaPasswordChange(VerificationRequest request) {
        User user = (User) repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiRequestException("user not exist", HttpStatus.NOT_FOUND));
        if(tfaService.isOtpNotValid(user.getSecret(),request.getCode())){
            return ResetPasswordResponse.builder()
                    .status(401)
                    .mfaEnabled(user.isMfaEnabled())
                    .message("invalid code")
                    .build();
        }
        return ResetPasswordResponse.builder()
                .status(200)
                .message("mfa verified")
                .secret(user.getPasswordResetToken())
                .rightAccount(true)
                .mfaEnabled(user.isMfaEnabled())
                .build();
    }
}

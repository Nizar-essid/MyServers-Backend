package com.myservers.backend.security.auth.services;

import com.myservers.backend.security.auth.entities.Admin;
import com.myservers.backend.security.auth.repositories.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    public Admin getAdminByEmail(String email) {
        Optional<Admin> admin = adminRepository.findByEmail(email);
        return admin.orElse(null);
    }

    public Optional<Admin> findAdminByEmail(String email) {
        return adminRepository.findByEmail(email);
    }
}

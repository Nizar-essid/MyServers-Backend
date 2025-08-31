package com.myservers.backend.users.services;

import com.myservers.backend.servers.entities.Server;
import com.myservers.backend.users.entities.UserPrice;
import com.myservers.backend.users.repositories.UserPriceRepository;
import com.myservers.backend.security.auth.entities.Admin;
import com.myservers.backend.security.auth.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserPriceService {

    @Autowired
    private UserPriceRepository userPriceRepository;

    public List<UserPrice> getAllActiveUserPrices() {
        return userPriceRepository.findAllActiveUserPrices();
    }

    public List<UserPrice> getPricesByUser(User user) {
        return userPriceRepository.findByUserAndIsActiveTrue(user);
    }

    public List<UserPrice> getPricesByServer(Server server) {
        return userPriceRepository.findByServerAndIsActiveTrue(server);
    }

    public List<UserPrice> getPricesByUserAndServer(User user, Server server) {
        return userPriceRepository.findByUserAndServer(user, server);
    }

    public Optional<UserPrice> getPriceByUserServerAndDuration(User user, Server server, Integer durationDays) {
        return userPriceRepository.findByUserAndServerAndDurationDaysAndIsActiveTrue(user, server, durationDays);
    }

    public UserPrice createUserPrice(UserPrice userPrice, Admin createdBy) {
        userPrice.setDateCreation(new Date());
        userPrice.setCreatedBy(createdBy);
        userPrice.setIsActive(true);
        return userPriceRepository.save(userPrice);
    }

    public UserPrice updateUserPrice(Long id, UserPrice updatedPrice, Admin updatedBy) {
        Optional<UserPrice> existingPrice = userPriceRepository.findById(id);
        if (existingPrice.isPresent()) {
            UserPrice price = existingPrice.get();
            price.setPrice(updatedPrice.getPrice());
            price.setDurationDays(updatedPrice.getDurationDays());
            price.setIsActive(updatedPrice.getIsActive());
            price.setLatestUpdate(new Date());
            price.setUpdatedBy(updatedBy);
            return userPriceRepository.save(price);
        }
        throw new RuntimeException("User price not found with id: " + id);
    }

    public boolean deleteUserPrice(Long id) {
        Optional<UserPrice> price = userPriceRepository.findById(id);
        if (price.isPresent()) {
            userPriceRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean deactivateUserPrice(Long id) {
        Optional<UserPrice> price = userPriceRepository.findById(id);
        if (price.isPresent()) {
            UserPrice userPrice = price.get();
            userPrice.setIsActive(false);
            userPriceRepository.save(userPrice);
            return true;
        }
        return false;
    }

    public Optional<UserPrice> getPriceById(Long id) {
        return userPriceRepository.findById(id);
    }
}

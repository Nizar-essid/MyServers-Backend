package com.myservers.backend.users.services;

import com.myservers.backend.servers.entities.Server;
import com.myservers.backend.users.entities.GroupPrice;
import com.myservers.backend.users.entities.UserGroup;
import com.myservers.backend.users.repositories.GroupPriceRepository;
import com.myservers.backend.security.auth.entities.Admin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class GroupPriceService {

    @Autowired
    private GroupPriceRepository groupPriceRepository;

    public List<GroupPrice> getAllActivePrices() {
        return groupPriceRepository.findAllActivePrices();
    }

    public List<GroupPrice> getPricesByGroup(UserGroup userGroup) {
        return groupPriceRepository.findByUserGroupAndIsActiveTrue(userGroup);
    }

    public List<GroupPrice> getPricesByServer(Server server) {
        return groupPriceRepository.findByServerAndIsActiveTrue(server);
    }

    public List<GroupPrice> getPricesByGroupAndServer(UserGroup userGroup, Server server) {
        return groupPriceRepository.findByUserGroupAndServer(userGroup, server);
    }

    public Optional<GroupPrice> getPriceByGroupServerAndDuration(UserGroup userGroup, Server server, Integer durationDays) {
        return groupPriceRepository.findByUserGroupAndServerAndDurationDaysAndIsActiveTrue(userGroup, server, durationDays);
    }

    public GroupPrice createGroupPrice(GroupPrice groupPrice, Admin createdBy) {
        groupPrice.setDateCreation(new Date());
        groupPrice.setCreatedBy(createdBy);
        groupPrice.setIsActive(true);
        return groupPriceRepository.save(groupPrice);
    }

    public GroupPrice updateGroupPrice(Long id, GroupPrice updatedPrice, Admin updatedBy) {
        Optional<GroupPrice> existingPrice = groupPriceRepository.findById(id);
        if (existingPrice.isPresent()) {
            GroupPrice price = existingPrice.get();
            price.setPrice(updatedPrice.getPrice());
            price.setDurationDays(updatedPrice.getDurationDays());
            price.setIsActive(updatedPrice.getIsActive());
            price.setLatestUpdate(new Date());
            price.setUpdatedBy(updatedBy);
            return groupPriceRepository.save(price);
        }
        throw new RuntimeException("Group price not found with id: " + id);
    }

    public boolean deleteGroupPrice(Long id) {
        Optional<GroupPrice> price = groupPriceRepository.findById(id);
        if (price.isPresent()) {
            groupPriceRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean deactivateGroupPrice(Long id) {
        Optional<GroupPrice> price = groupPriceRepository.findById(id);
        if (price.isPresent()) {
            GroupPrice groupPrice = price.get();
            groupPrice.setIsActive(false);
            groupPriceRepository.save(groupPrice);
            return true;
        }
        return false;
    }

    public Optional<GroupPrice> getPriceById(Long id) {
        return groupPriceRepository.findById(id);
    }
}

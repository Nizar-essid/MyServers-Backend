package com.myservers.backend.users.services;

import com.myservers.backend.users.entities.UserGroup;
import com.myservers.backend.users.repositories.UserGroupRepository;
import com.myservers.backend.security.auth.entities.Admin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserGroupService {

    @Autowired
    private UserGroupRepository userGroupRepository;

    public List<UserGroup> getAllActiveGroups() {
        return userGroupRepository.findAllActiveGroups();
    }

    public List<UserGroup> getAllGroups() {
        return userGroupRepository.findAll();
    }

    public Optional<UserGroup> getGroupById(Long id) {
        return userGroupRepository.findById(id);
    }

    public Optional<UserGroup> getGroupByName(String name) {
        return userGroupRepository.findByName(name);
    }

    public UserGroup createGroup(UserGroup userGroup, Admin createdBy) {
        userGroup.setDateCreation(new Date());
        userGroup.setCreatedBy(createdBy);
        userGroup.setIsActive(true);
        return userGroupRepository.save(userGroup);
    }

    public UserGroup updateGroup(Long id, UserGroup updatedGroup, Admin updatedBy) {
        Optional<UserGroup> existingGroup = userGroupRepository.findById(id);
        if (existingGroup.isPresent()) {
            UserGroup group = existingGroup.get();
            group.setName(updatedGroup.getName());
            group.setDescription(updatedGroup.getDescription());
            group.setDiscountPercentage(updatedGroup.getDiscountPercentage());
            group.setIsActive(updatedGroup.getIsActive());
            group.setLatestUpdate(new Date());
            group.setUpdatedBy(updatedBy);
            return userGroupRepository.save(group);
        }
        throw new RuntimeException("User group not found with id: " + id);
    }

    public boolean deleteGroup(Long id) {
        Optional<UserGroup> group = userGroupRepository.findById(id);
        if (group.isPresent()) {
            userGroupRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean deactivateGroup(Long id) {
        Optional<UserGroup> group = userGroupRepository.findById(id);
        if (group.isPresent()) {
            UserGroup userGroup = group.get();
            userGroup.setIsActive(false);
            userGroupRepository.save(userGroup);
            return true;
        }
        return false;
    }

    public boolean activateGroup(Long id) {
        Optional<UserGroup> group = userGroupRepository.findById(id);
        if (group.isPresent()) {
            UserGroup userGroup = group.get();
            userGroup.setIsActive(true);
            userGroupRepository.save(userGroup);
            return true;
        }
        return false;
    }

    public boolean existsByName(String name) {
        return userGroupRepository.existsByName(name);
    }
}

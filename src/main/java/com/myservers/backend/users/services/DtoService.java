package com.myservers.backend.users.services;

import com.myservers.backend.security.auth.entities.User;
import com.myservers.backend.users.dto.UserDto;
import com.myservers.backend.users.dto.UserGroupDto;
import com.myservers.backend.users.entities.UserGroup;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DtoService {

    public UserDto convertToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .telephone(user.getTelephone())
                .balance(user.getBalance())
                .state(user.isState())
                .role(user.getRole().name())
                .mfaEnabled(user.isMfaEnabled())
                .build();
    }

    public List<UserDto> convertToUserDtoList(List<User> users) {
        return users.stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
    }

    public UserGroupDto convertToUserGroupDto(UserGroup userGroup) {
        return UserGroupDto.builder()
                .id(userGroup.getId())
                .name(userGroup.getName())
                .description(userGroup.getDescription())
                .discountPercentage(userGroup.getDiscountPercentage())
                .isActive(userGroup.getIsActive())
                .build();
    }

    public List<UserGroupDto> convertToUserGroupDtoList(List<UserGroup> userGroups) {
        return userGroups.stream()
                .map(this::convertToUserGroupDto)
                .collect(Collectors.toList());
    }
}

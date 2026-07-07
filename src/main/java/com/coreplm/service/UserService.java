package com.coreplm.service;

import com.coreplm.dto.UserCreateRequest;
import com.coreplm.dto.UserResponse;

import java.util.List;
import java.util.Set;

public interface UserService {

    UserResponse createUser(UserCreateRequest request);

    UserResponse getUserById(Long id);

    List<UserResponse> getAllUsers();

    void deleteUser(Long id);

    UserResponse updateUserRoles(Long id, Set<String> roleNames);
}
package com.coreplm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.coreplm.dto.UserCreateRequest;
import com.coreplm.dto.UserResponse;
import com.coreplm.entity.Role;
import com.coreplm.entity.User;
import com.coreplm.exception.ResourceNotFoundException;
import com.coreplm.repository.RoleRepository;
import com.coreplm.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

	 private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class); 
	 
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {

        // Check if username already exists
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException(
                    "Username already exists: " + request.username());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException(
                    "Email already exists: " + request.email());
        }

        // Create User entity
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFullName(request.fullName());

        // Encode password
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        // Set default active status
        user.setActive(true);

        // Assign roles
        user.setRoles(resolveRoles(request.roleNames()));

        // Save user
        User savedUser = userRepository.save(user);

        log.info("User created: id={}, username={}", savedUser.getId(), savedUser.getUsername());
        // Return response
        return mapToResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        return mapToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setActive(false);
        userRepository.save(user);
        log.info("User deactivated: id={}, username={}", user.getId(), user.getUsername());
    }

    /**
     * Convert role names received from request
     * into Role entities from database.
     */
    private Set<Role> resolveRoles(Set<String> roleNames) {

        if (roleNames == null || roleNames.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one role must be assigned.");
        }

        Set<Role> roles = new HashSet<>();

        for (String roleName : roleNames) {

            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Role not found: " + roleName));

            roles.add(role);
        }

        return roles;
    }

    /**
     * Convert User Entity to UserResponse DTO.
     */
    private UserResponse mapToResponse(User user) {

        Set<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.isActive(),
                roles,
                user.getCreatedAt()
        );
    }
}
package com.coreplm.service;

import com.coreplm.dto.UserCreateRequest;
import com.coreplm.dto.UserResponse;
import com.coreplm.entity.Role;
import com.coreplm.entity.User;
import com.coreplm.exception.ResourceNotFoundException;
import com.coreplm.repository.RoleRepository;
import com.coreplm.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final String DEFAULT_REGISTRATION_ROLE = "VIEWER";

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

        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException(
                    "Username already exists: " + request.username());
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException(
                    "Email already exists: " + request.email());
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setActive(true);

        // Security fix: public registration always gets the default
        // minimal role. Clients cannot self-assign roles at signup.
        Role defaultRole = roleRepository.findByName(DEFAULT_REGISTRATION_ROLE)
                .orElseThrow(() -> new IllegalStateException(
                        "Default role not seeded: " + DEFAULT_REGISTRATION_ROLE));
        user.setRoles(Set.of(defaultRole));

        User savedUser = userRepository.save(user);

        log.info("User created: id={}, username={}", savedUser.getId(), savedUser.getUsername());

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
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        user.setActive(false);
        userRepository.save(user);

        log.info("User deactivated: id={}, username={}", user.getId(), user.getUsername());
    }

    @Override
    @Transactional
    public UserResponse updateUserRoles(Long id, Set<String> roleNames) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        user.setRoles(resolveRoles(roleNames));

        User savedUser = userRepository.save(user);

        log.info("Roles updated for user: id={}, username={}, newRoles={}",
                savedUser.getId(), savedUser.getUsername(), roleNames);

        return mapToResponse(savedUser);
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
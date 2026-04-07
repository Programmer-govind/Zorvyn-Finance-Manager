package com.zorvyn.finance.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zorvyn.finance.dto.request.CreateUserRequest;
import com.zorvyn.finance.dto.request.UpdateUserRequest;
import com.zorvyn.finance.dto.response.UserResponse;
import com.zorvyn.finance.enums.Role;
import com.zorvyn.finance.exception.DuplicateResourceException;
import com.zorvyn.finance.exception.ResourceNotFoundException;
import com.zorvyn.finance.mappers.UserMapper;
import com.zorvyn.finance.model.User;
import com.zorvyn.finance.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "A user with email " + request.getEmail() + " already exists"
            );
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .active(true)
                .deleted(false)
                .build();

        return UserMapper.toResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers(Role role, Boolean active) {
        return userRepository.findAllFiltered(role, active)
                .stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return UserMapper.toResponse(findUserOrThrow(id));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = findUserOrThrow(id);
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getActive()   != null) user.setActive(request.getActive());
        return UserMapper.toResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public void deleteUser(Long id) {
        findUserOrThrow(id);
        userRepository.softDeleteById(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public UserResponse changeUserRole(Long id, Role newRole) {
        User user = findUserOrThrow(id);
        user.setRole(newRole);
        return UserMapper.toResponse(userRepository.save(user));
    }

    public User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id
                ));
    }
}
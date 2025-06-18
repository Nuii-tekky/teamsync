package com.teamsync.service;

import com.teamsync.entity.User;
import com.teamsync.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

// Service layer for user-related business logic
@Service
public class UserService {

    private static final Logger logger = Logger.getLogger(UserService.class.getName());
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Save a user with validation
    public User saveUser(User user) {
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("User email cannot be empty");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("User password cannot be empty");
        }
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("User first name cannot be empty");
        }
        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("User last name cannot be empty");
        }
        logger.info("Saving user: " + user.getEmail());
        return userRepository.save(user);
    }

    // Retrieve a user by ID
    public Optional<User> getUserById(UUID id) {
        logger.info("Retrieving user with ID: " + id);
        return userRepository.findById(id);
    }

    // Retrieve a user by email
    public Optional<User> getUserByEmail(String email) {
        logger.info("Retrieving user with email: " + email);
        return userRepository.findByEmail(email);
    }

    // Retrieve all users
    public List<User> getAllUsers() {
        logger.info("Retrieving all users");
        try {
            List<User> users = userRepository.findAll();
            if (users.isEmpty()) {
                logger.info("No users found");
            }
            return users;
        } catch (Exception ex) {
            logger.severe("Error retrieving users: " + ex.getMessage());
            throw new RuntimeException("Failed to retrieve users", ex);
        }
    }

    // Delete a user by ID
    public void deleteUser(UUID id) {
        logger.info("Deleting user with ID: " + id);
        userRepository.deleteById(id);
    }
}
package com.teamsync.controller;

import com.teamsync.config.JwtUtil;
import com.teamsync.dto.ApiResponse;
import com.teamsync.dto.UserRegistrationDTO;
import com.teamsync.entity.User;
// import com.teamsync.mapper.UserMapper;
import com.teamsync.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = Logger.getLogger(AuthController.class.getName());

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
        logger.info("Registering user: " + registrationDTO.getEmail());
        User user = new User();
        user.setEmail(registrationDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setFirstName(registrationDTO.getFirstname());
        user.setLastName(registrationDTO.getLastname());
        User savedUser = userService.saveUser(user);
        String token = jwtUtil.generateToken(savedUser.getId(), savedUser.getEmail());
        Map<String, Object> responseData = new HashMap<>();
        // Manual mapping to avoid mapper issue
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", savedUser.getId());
        userData.put("email", savedUser.getEmail());
        userData.put("firstName", savedUser.getFirstName());
        userData.put("lastName", savedUser.getLastName());
        userData.put("createdAt", savedUser.getCreatedAt() != null ? savedUser.getCreatedAt().toString() : null);
        responseData.put("user", userData);
        responseData.put("token", token);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", responseData));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Login attempt for: " + loginRequest.getEmail());
        return userService.getUserByEmail(loginRequest.getEmail())
                .filter(user -> passwordEncoder.matches(loginRequest.getPassword(), user.getPassword()))
                .map(user -> {
                    String token = jwtUtil.generateToken(user.getId(), user.getEmail());
                    Map<String, Object> responseData = new HashMap<>();
                    // Manual mapping
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("id", user.getId());
                    userData.put("email", user.getEmail());
                    userData.put("firstName", user.getFirstName());
                    userData.put("lastName", user.getLastName());
                    userData.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
                    responseData.put("user", userData);
                    responseData.put("token", token);
                    return ResponseEntity.ok(ApiResponse.success("Login successful", responseData));
                })
                .orElseGet(() -> ResponseEntity.status(401)
                        .body(ApiResponse.error("Invalid email or password", null)));
    }
}  

class LoginRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}


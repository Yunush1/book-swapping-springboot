package com.book.swap.services.auth;

import com.book.swap.models.dto.AuthResponse;
import com.book.swap.models.dto.LoginDTO;
import com.book.swap.models.dto.UserDTO;
import com.book.swap.models.entities.DbUsers;
import com.book.swap.repository.UserRepository;
import com.book.swap.security.JwtService;
import com.book.swap.services.exceptions.InvalidCredentialsException;
import com.book.swap.services.exceptions.NotFoundException;
import com.book.swap.services.exceptions.UserAlreadyExistsException;
import com.book.swap.utils.books.InMemoryCacheService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final InMemoryCacheService cacheService;
    Logger logger = LoggerFactory.getLogger(AuthService.class);
    /**
     * Register a new user
     * @param userDTO User registration data
     * @return Created user DTO
     * @throws UserAlreadyExistsException if email or username already exists
     */
    @Transactional
    public UserDTO register(UserDTO userDTO) {
        logger.info("Attempting to register user with email: {}", userDTO.getEmail());

        // Check if user already exists
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            logger.warn("Registration failed: Email already exists - {}", userDTO.getEmail());
            throw new UserAlreadyExistsException("Email already registered: " + userDTO.getEmail());
        }

        if (userRepository.existsByUsername(userDTO.getUserName())) {
            logger.warn("Registration failed: Username already exists - {}", userDTO.getUserName());
            throw new UserAlreadyExistsException("Username already taken: " + userDTO.getUserName());
        }
        try {
            DbUsers dbUsers = DbUsers.builder()
                    .email(userDTO.getEmail())
                    .fullName(userDTO.getFullName())
                    .password(passwordEncoder.encode(userDTO.getPassword()))
                    .username(userDTO.getUserName())
                    .phoneNumber(userDTO.getPhoneNumber())
                    .roles(Set.of(userDTO.getRole()))
                    .build();

            DbUsers savedUser = userRepository.save(dbUsers);
            logger.info("User registered successfully with ID: {}",savedUser.getId());

            return convertToDTO(savedUser);
        } catch (Exception e) {
            logger.error("Error during user registration: {}", e.getMessage());
            throw new RuntimeException("Failed to register user", e);
        }
    }

    /**
     * Authenticate user with email/username and password
     * @param loginDTO Login credentials
     * @return Login response with tokens and user details
     * @throws NotFoundException if user doesn't exist
     * @throws InvalidCredentialsException if password is incorrect
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginDTO loginDTO) throws Exception {
        logger.info("Login attempt for identifier: {}", loginDTO.getIdentifier());

        String identifier = loginDTO.getIdentifier();
        String password = loginDTO.getPassword();

        // Validate input
        validateLoginInput(identifier, password);

        // Find user by email or username
        DbUsers dbUsers = userRepository.findByEmail(identifier)
                .orElseThrow(() -> {
                    logger.warn("Login failed: User not found : {}", identifier);
                    return new NotFoundException("Invalid credentials");
                });

        // Verify password
        if (!passwordEncoder.matches(password, dbUsers.getPassword())) {
            logger.warn("Login failed: Invalid password for user: {}", identifier);
            throw new InvalidCredentialsException("Invalid credentials");
        }

        // Clear session
        if (dbUsers.getSessionId() != null) {
            cacheService.invalidateSession(dbUsers.getSessionId());
            logger.info("Session expired for user: {}", dbUsers.getSessionId());
        }

        // Generate unique session ID using UUID for better uniqueness
        String sessionId = UUID.randomUUID().toString();

        // Update user session
        dbUsers.setSessionId(sessionId);
        dbUsers.setLastLoginAt(LocalDateTime.now()); // Track last login
        userRepository.save(dbUsers);

        // Cache session for quick validation
        cacheService.storeSession(sessionId, dbUsers);

        // Generate tokens with session ID embedded
        String accessToken = jwtService.generateAccessToken(dbUsers, sessionId);
        String refreshToken = jwtService.generateRefreshToken(dbUsers, sessionId);

        logger.info("User logged in successfully: {} with session: {}", dbUsers.getUsername(), sessionId);

        return AuthResponse.builder()
                .success(true)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(convertToDTO(dbUsers))
                .build();
    }

    private void validateLoginInput(String identifier, String password) {
        if (identifier == null || identifier.trim().isEmpty()) {
            logger.error("Identifier is null or empty");
            throw new IllegalArgumentException("Identifier cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
    }

    /**
     * Convert entity to DTO
     * @param dbUsers Database user entity
     * @return User DTO
     */
    private UserDTO convertToDTO(DbUsers dbUsers) {
        return UserDTO.builder()
                .email(dbUsers.getEmail())
                .role(dbUsers.getRoles().iterator().next())
                .fullName(dbUsers.getFullName())
                .userName(dbUsers.getUsername())
                .phoneNumber(dbUsers.getPhoneNumber())
                .build();
    }
}
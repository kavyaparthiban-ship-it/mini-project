package com.crowdsense.service;

import com.crowdsense.dto.AuthLoginRequest;
import com.crowdsense.dto.AuthRegisterRequest;
import com.crowdsense.dto.AuthResponse;
import com.crowdsense.entity.PassengerCredentialEntity;
import com.crowdsense.entity.PassengerEntity;
import com.crowdsense.exception.InvalidCredentialsException;
import com.crowdsense.repository.PassengerCredentialRepository;
import com.crowdsense.repository.PassengerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;

@Service
public class AuthService {

    private static final int ITERATIONS = 310_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String HASH_ALGORITHM = "PBKDF2WithHmacSHA256";

    private final PassengerRepository passengerRepository;
    private final PassengerCredentialRepository credentialRepository;

    public AuthService(PassengerRepository passengerRepository,
                       PassengerCredentialRepository credentialRepository) {
        this.passengerRepository = passengerRepository;
        this.credentialRepository = credentialRepository;
    }

    @Transactional
    public AuthResponse register(AuthRegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (passengerRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("An account with this email already exists. Please sign in.");
        }

        PassengerEntity passenger = new PassengerEntity(request.name().trim(), email);
        passenger = passengerRepository.saveAndFlush(passenger);
        credentialRepository.save(new PassengerCredentialEntity(
                passenger.getPassengerId(), hashPassword(request.password())));

        return toResponse(passenger);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthLoginRequest request) {
        PassengerEntity passenger = passengerRepository.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(InvalidCredentialsException::new);
        PassengerCredentialEntity credential = credentialRepository.findById(passenger.getPassengerId())
                .orElseThrow(InvalidCredentialsException::new);

        if (!verifyPassword(request.password(), credential.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return toResponse(passenger);
    }

    private String hashPassword(String password) {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(salt);
        byte[] hash = deriveKey(password, salt, ITERATIONS);
        return ITERATIONS + ":" + Base64.getEncoder().encodeToString(salt)
                + ":" + Base64.getEncoder().encodeToString(hash);
    }

    private boolean verifyPassword(String password, String encodedHash) {
        String[] parts = encodedHash.split(":", -1);
        if (parts.length != 3) {
            return false;
        }

        try {
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[2]);
            if (iterations < 1 || salt.length == 0 || expectedHash.length == 0) {
                return false;
            }
            byte[] actualHash = deriveKey(password, salt, iterations);
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private byte[] deriveKey(String password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH_BITS);
        try {
            return SecretKeyFactory.getInstance(HASH_ALGORITHM).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Password hashing is unavailable.", ex);
        } finally {
            spec.clearPassword();
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private AuthResponse toResponse(PassengerEntity passenger) {
        return new AuthResponse(passenger.getPassengerId(), passenger.getPassengerName(), passenger.getEmail());
    }
}

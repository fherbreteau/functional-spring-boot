package io.github.fherbreteau.functional.security;

import java.util.List;

import io.github.fherbreteau.functional.driven.PasswordProtector;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.ValidationResult;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordProtectorImpl implements PasswordProtector {

    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;

    public PasswordProtectorImpl(PasswordEncoder passwordEncoder, PasswordValidator passwordValidator) {
        this.passwordEncoder = passwordEncoder;
        this.passwordValidator = passwordValidator;
    }

    @Override
    public String protect(String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public List<String> validate(String username, String password) {
        PasswordData passwordData = new PasswordData(username, password);
        ValidationResult result = passwordValidator.validate(passwordData);
        if (result.isValid()) {
            return List.of();
        }
        return result.getMessages();
    }
}

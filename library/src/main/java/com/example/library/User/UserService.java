package com.example.library.User;

import Exceptions.UserNotFoundException;
import com.example.library.security.AESEncryptionConverter;
import com.example.library.security.LoginAttemptService;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;



    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       LoginAttemptService loginAttemptService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptService = loginAttemptService;
    }


    public User createUser(User user) {

        //validera lösenordpolicy
        String rawPassword = user.getPassword();
        if(rawPassword == null || rawPassword.length() < 8
        || !rawPassword.matches(".*[0-9].*")
        || !rawPassword.matches(".*[A-Za-z].*")) {
            throw new IllegalArgumentException("Lösenord måste vara minst 8 tecken och innehålla både bokstäver och siffror");
        }

        //kontrollera att email inte redan finns
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Användarnamnet (email) är redan registrerat.");
        }

        // sanitize inputs
        user.setFirstName(Jsoup.clean(user.getFirstName(), Safelist.basic()));
        user.setLastName(Jsoup.clean(user.getLastName(), Safelist.basic()));
        user.setEmail(Jsoup.clean(user.getEmail(), Safelist.basic()));

        // Kryptera lösenord
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRoles("USER"); // nya användare får alltid USER-roll
        user.setEnabled(true);
        user.setRegistrationDate(LocalDate.now());

        // Kryptera SSN – sker via @Convert(AESEncryptionConverter),
        // kan fortfarande sanera input
//        if (user.getSsn() != null) {
//            user.setSsn(Jsoup.clean(user.getSsn(), Safelist.basic()));
//        }

        return userRepository.save(user);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}


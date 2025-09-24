package com.example.library.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);


//    // Praktiskt för att kolla om email redan finns
    boolean existsByEmail(String email);
}

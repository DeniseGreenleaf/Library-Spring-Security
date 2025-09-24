package com.example.library.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        //hämta användare från databas
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found" + email));

        //konvertera till spring security's UserDetails
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail()) // Använd email som username
                .password(user.getPassword())                 // Krypterat lösenord från DB
                .authorities("ROLE_" + user.getRoles())       // Spring Security kräver "ROLE_" prefix
                .disabled(!user.isEnabled())                 // Invertera enabled för disabled
                .build();
    }
}



package com.example.library.User;

import com.example.library.security.AESEncryptionConverter;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "first_name", length = 255)
    @NotBlank(message = "Förnamn är obligatoriskt")
    private String firstName;

    @Column(name = "last_name", length = 255)
    @NotBlank(message = "Efternamn är obligatoriskt")
    private String lastName;

    @Column(name = "email", unique = true, length = 255, nullable = false)
    @NotBlank(message = "Email är obligatoriskt")
    @Email(message = "Felaktigt emailformat")

    private String email;

    @Column(name = "roles", length = 255)
    private String roles;

    @Column(name = "enabled")
    private boolean enabled = true;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // skrivbart, men visas aldrig i JSON-svar
    @Column(name = "password", nullable = false)
    @NotBlank(message = "Lösenord är obligatoriskt")
    @Size(min = 8, message = "Lösenord måste vara minst 8 tecken")
    private String password;


    @Column(name = "registration_date")
    private LocalDate registrationDate;

//    @Convert(converter = AESEncryptionConverter.class)
//    @Column(name = "ssn", length = 500)
//    @Pattern(regexp = "^(\\d{6}-\\d{4}|\\d{8}-\\d{4}|\\d{10}|\\d{12})?$",
//            message = "Personnummer måste vara i format XXXXXX-XXXX, XXXXXXXX-XXXX, XXXXXXXXXX eller XXXXXXXXXXXX")
//
//    private String ssn; // personnummer

    public User() {}

    public User(Long userId, String firstName, String lastName, String email, String password, LocalDate registrationDate) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.registrationDate = registrationDate;
    }

    public User(String email, String password, String roles) {
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.enabled = true;
    }

    // Getters & setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }



    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

//    public String getSsn() {
//        return ssn;
//    }
//
//    public void setSsn(String ssn) {
//        this.ssn = ssn;
//    }

    public List<String> getRoleList() {
        if (this.roles != null && !this.roles.isEmpty()) {
            return List.of(this.roles.split(",")); // dela på kommatecken
        }
        return List.of();
    }



    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", registrationDate=" + registrationDate +
                '}';
    }
}

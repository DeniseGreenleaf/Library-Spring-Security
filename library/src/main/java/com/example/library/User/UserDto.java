package com.example.library.User;

public class UserDto {
    private Long id;
    private String name;
    private String email;

    public UserDto() {}

    public UserDto(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public UserDto(User user) {
        this.id = user.getUserId();
        this.name = user.getFirstName() + " " + user.getLastName();
        this.email = user.getEmail();
    }


    // Getters och Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

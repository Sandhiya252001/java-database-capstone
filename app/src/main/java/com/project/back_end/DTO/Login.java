package com.example.dto;

public class Login {

    private String identifier;
    private String password;

    // ✅ Default Constructor
    public Login() {}

    // ✅ Parameterized Constructor
    public Login(String identifier, String password) {
        this.identifier = identifier;
        this.password = password;
    }

    // ✅ Getter and Setter for identifier
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    // ✅ Getter and Setter for password
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

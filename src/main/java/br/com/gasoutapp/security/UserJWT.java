package br.com.gasoutapp.security;

import lombok.Data;

@Data
public class UserJWT {

    private String id;
    private String login;
    private Long expiresIn;

    public UserJWT(String id, String login, Long expiresIn) {
        this.id = id;
        this.login = login;
        this.expiresIn = expiresIn;
    }
}
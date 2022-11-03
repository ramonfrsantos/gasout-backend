package br.com.gasoutapp.security;

import lombok.Data;

@Data
public class UserJWT {

    private String id;
    private String login;

    public UserJWT(String id, String login) {
        this.id = id;
        this.login = login;
    }
}
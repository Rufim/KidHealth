package ru.constant.kidhealth.domain.models;

import lombok.Data;

@Data
public class Credentials {
    private String login;
    private String password;
    private String userId;
    private String refreshToken;

    public Credentials(){ }

    public Credentials(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public Credentials(Token token) {
        userId = token.getAccount().getId();
        refreshToken = token.getRefreshToken();
    }
}

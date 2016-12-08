package com.example.valeriyasin.authorization;

/**
 * Created by valeriyasin on 12/7/16.
 */
public class TokenObject {
    private String token;
    private String expirationDate;

    TokenObject(String token, String expirationDate) {
        this.expirationDate = expirationDate;
        this.token = token;
    }



    public String getExpirationDate() {
        return expirationDate;
    }

    public String getToken() {
        return token;
    }
}

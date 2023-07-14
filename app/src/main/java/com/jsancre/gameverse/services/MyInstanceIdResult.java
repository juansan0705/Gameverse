package com.jsancre.gameverse.services;

public class MyInstanceIdResult {private final String token;

    public MyInstanceIdResult(String token) {
        this.token = token;
    }

    public String getToken() {
        return this.token;
    }
}
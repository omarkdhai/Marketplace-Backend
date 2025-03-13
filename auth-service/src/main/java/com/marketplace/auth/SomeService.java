package com.marketplace.auth;

import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

public class SomeService {

    @Inject
    JsonWebToken jwt;

    public void printUserInfo() {
        String username = jwt.getName();
        System.out.println("Authenticated as: " + username);
    }
}

package com.marketplace.auth.Service;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class KeycloakService {

    private Keycloak keycloak;



    @ConfigProperty(name = "keycloak.serverURL")
    private String serverUrl;
    @ConfigProperty(name = "keycloak.realm")
    private String realm;
    @ConfigProperty(name = "keycloak.clientId")
    private String clientId;
    @ConfigProperty(name = "keycloak.clientSecret")
    String clientSecret;
    /*@ConfigProperty(name = "keycloak.username")
    private String username;
    @ConfigProperty(name = "keycloak.password")
    private String password;*/

    @PostConstruct
    void init() {
        keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
    }

    public String createUser(String firstName, String lastName, String email, String password, String birthdate) {
        UserRepresentation user = new UserRepresentation();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setUsername(email);
        user.setEnabled(true);
        user.setAttributes(Collections.singletonMap("birthdate", List.of(birthdate)));

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        user.setCredentials(List.of(credential));

        Response response = keycloak.realm(realm).users().create(user);
        if (response.getStatus() != 201) {
            throw new WebApplicationException("User creation failed: " + response.getStatus(), response.getStatus());
        }

        return CreatedResponseUtil.getCreatedId(response);
    }

    public List<UserRepresentation> getAllUsers() {
        return keycloak.realm(realm).users().list();
    }
}
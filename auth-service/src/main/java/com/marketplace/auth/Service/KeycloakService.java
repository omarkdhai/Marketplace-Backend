package com.marketplace.auth.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.*;

@ApplicationScoped
public class KeycloakService {

    private Client client;
    private Keycloak keycloak;


    @ConfigProperty(name = "keycloak.serverURL")
    private String serverUrl;

    @ConfigProperty(name = "keycloak.realm")
    private String realm;

    @ConfigProperty(name = "keycloak.clientId")
    private String clientId;

    @ConfigProperty(name = "keycloak.clientSecret")
    private String clientSecret;

    @ConfigProperty(name = "mail.username")
    String emailUsername;

    @ConfigProperty(name = "mail.password")
    String emailPassword;

    @PostConstruct
    void init() {
        client = ClientBuilder.newClient();
        keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .resteasyClient(client)
                .build();
    }

    @PreDestroy
    void cleanup() {
        if (client != null) {
            client.close();
        }
        if (keycloak != null) {
            keycloak.close();
        }
    }

    public String createUser(String firstName, String lastName, String email, String password, String birthdate) {
        List<UserRepresentation> existingUsers = keycloak.realm(realm).users().search(email);
        if (!existingUsers.isEmpty()) {
            throw new WebApplicationException("User already exists with this email", 409);
        }

        UserRepresentation user = new UserRepresentation();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setUsername(email);
        user.setEnabled(true);
        user.setEmailVerified(false);

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("birthdate", List.of(birthdate));
        user.setAttributes(attributes);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        user.setCredentials(List.of(credential));

        Response response = keycloak.realm(realm).users().create(user);
        if (response.getStatus() != 201) {
            throw new WebApplicationException("User creation failed: " + response.getStatus(), response.getStatus());
        }

        String userId = CreatedResponseUtil.getCreatedId(response);

        sendVerificationEmail(userId);

        return userId;
    }

    public void sendVerificationEmail(String userId) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(userId);
            userResource.sendVerifyEmail();
        } catch (Exception e) {
            throw new RuntimeException("Failed to send verification email: " + e.getMessage(), e);
        }
    }

    public AccessTokenResponse login(String email, String password) {
        List<UserRepresentation> users = keycloak.realm(realm).users().search(email);

        if (users.isEmpty()) {
            throw new WebApplicationException("User not found", Response.Status.NOT_FOUND);
        }

        UserRepresentation user = users.get(0);

        if (!Boolean.TRUE.equals(user.isEmailVerified())) {
            throw new WebApplicationException("Email not verified. Please check your inbox.", Response.Status.FORBIDDEN);
        }

        try {
            Form form = new Form()
                    .param("client_id", clientId)
                    .param("client_secret", clientSecret)
                    .param("username", email)
                    .param("password", password)
                    .param("grant_type", "password");

            String tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

            AccessTokenResponse tokenResponse = client.target(tokenUrl)
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.form(form), AccessTokenResponse.class);

            return tokenResponse;

        } catch (Exception e) {
            throw new WebApplicationException("Invalid credentials", Response.Status.UNAUTHORIZED);
        }
    }

    public List<UserRepresentation> getAllUsers() {
        return keycloak.realm(realm).users().list();
    }

    public void updateUserAttributes(String userId, Map<String, String> newAttributes) {
        UserResource userResource = keycloak.realm(realm).users().get(userId);
        UserRepresentation user = userResource.toRepresentation();

        Map<String, List<String>> updatedAttributes = user.getAttributes() != null
                ? new HashMap<>(user.getAttributes())
                : new HashMap<>();

        List<String> allowedFields = List.of("phone", "street", "city", "postalCode");

        for (String key : allowedFields) {
            if (newAttributes.containsKey(key)) {
                updatedAttributes.put(key, List.of(newAttributes.get(key)));
            }
        }

        user.setAttributes(updatedAttributes);
        userResource.update(user);
    }

    public void deleteUser(String userId) {
        try {
            keycloak.realm(realm).users().get(userId).remove();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete user: " + e.getMessage(), e);
        }
    }

    public void codeEmailVerif(String email) {
        List<UserRepresentation> users = keycloak.realm(realm).users().search(email);

        if (users.isEmpty()) {
            throw new WebApplicationException("User not found", Response.Status.NOT_FOUND);
        }

        UserRepresentation user = users.get(0);
        String userId = user.getId();

        try {
            // 1. Generate random 4-digit code
            String verificationCode = String.format("%04d", new Random().nextInt(10000));

            // 2. Store code and expiry time (15 minutes) as user attributes
            UserResource userResource = keycloak.realm(realm).users().get(userId);
            UserRepresentation userRep = userResource.toRepresentation();
            Map<String, List<String>> attributes = userRep.getAttributes() != null
                    ? new HashMap<>(userRep.getAttributes())
                    : new HashMap<>();

            attributes.put("verification_code", List.of(verificationCode));
            attributes.put("verification_expiry", List.of(String.valueOf(System.currentTimeMillis() + 900_000)));

            userRep.setAttributes(attributes);
            userResource.update(userRep);

            // 3. Send real email
            sendEmail(email, verificationCode);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send verification email: " + e.getMessage(), e);
        }
    }

    private void sendEmail(String recipientEmail, String code) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailUsername, emailPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailUsername));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(recipientEmail)
            );
            message.setSubject("Password Reset Verification Code");
            message.setText("Your verification code is: " + code + "\n\nClick here to access to verify your identity: http://localhost:4200/security-code");

            Transport.send(message);

            System.out.println("Verification email sent successfully to: " + recipientEmail);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }


}

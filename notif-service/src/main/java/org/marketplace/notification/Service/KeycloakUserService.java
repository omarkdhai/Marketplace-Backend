package org.marketplace.notification.Service;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class KeycloakUserService {

    private static final Logger LOG = Logger.getLogger(KeycloakUserService.class);

    @Inject
    Keycloak keycloakAdminClient;

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

    public Optional<UserRepresentation> findUserById(String userId) {
        try {
            UserResource userResource = keycloakAdminClient.realm(realm).users().get(userId);
            if (userResource != null) {
                return Optional.of(userResource.toRepresentation());
            }
        } catch (NotFoundException nfe) {
            LOG.warnf("User not found in Keycloak with ID: %s", userId);
        } catch (Exception e) {
            LOG.errorf(e, "Error fetching user %s from Keycloak", userId);
        }
        return Optional.empty();
    }

    public Optional<String> getUserEmail(String userId) {
        return findUserById(userId).map(UserRepresentation::getEmail);
    }

    public Map<String, List<String>> getUserAttributes(String userId) {
        return findUserById(userId)
                .map(UserRepresentation::getAttributes)
                .orElse(Collections.emptyMap());
    }

    public boolean updateUserAttributes(String userId, Map<String, List<String>> attributes) {
        try {
            UserResource userResource = keycloakAdminClient.realm(realm).users().get(userId);
            if (userResource == null) {
                LOG.warnf("Cannot update attributes. User not found: %s", userId);
                return false;
            }
            UserRepresentation userRepresentation = userResource.toRepresentation();
            // Merge attributes carefully - replace existing ones, add new ones
            Map<String, List<String>> existingAttributes = userRepresentation.getAttributes() != null ?
                    userRepresentation.getAttributes() :
                    Collections.emptyMap();

            // Create a mutable copy and update
            Map<String, List<String>> updatedAttributes = new java.util.HashMap<>(existingAttributes);
            updatedAttributes.putAll(attributes);

            userRepresentation.setAttributes(updatedAttributes);
            userResource.update(userRepresentation);
            LOG.infof("Successfully updated attributes for user %s", userId);
            return true;
        } catch (NotFoundException nfe) {
            LOG.warnf("Cannot update attributes. User not found: %s", userId);
            return false;
        } catch (Exception e) {
            LOG.errorf(e, "Error updating attributes for user %s", userId);
            return false;
        }
    }
}

package com.github.amiv1.keycloak;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

/**
 * Base class for Keycloak integration test classes.
 * <p>
 * Provides a pre-configured Keycloak container for integration tests to run with.
 */
public abstract class KeycloakIntegrationTest {
    protected static KeycloakDevContainer sKeycloakContainer;

    @BeforeAll
    public static void setUp() {
        sKeycloakContainer = new KeycloakDevContainer();
        sKeycloakContainer.start();
    }

    @AfterAll
    public static void destroy() {
        sKeycloakContainer.stop();
    }

    protected String getAuthServerUrl() {
        return sKeycloakContainer.getAuthServerUrl();
    }

    protected String getRealmUrl(String realm) {
        return getAuthServerUrl() + "/realms/" + realm ;
    }

    protected String getTokenUrl(String realm) {
        return getRealmUrl(realm) + "/protocol/openid-connect/token";
    }

    protected String getUserInfoUrl(String realm) {
        return getRealmUrl(realm) + "/protocol/openid-connect/userinfo";
    }

    protected String getIntrospectUrl(String realm) {
        return getRealmUrl(realm) + "/protocol/openid-connect/token/introspect";
    }
}

package com.example;

import dasniko.testcontainers.keycloak.ExtendableKeycloakContainer;
import org.testcontainers.utility.MountableFile;

import java.util.Arrays;

/**
 * Customised Keycloak Docker container for extension development and debugging.
 */
public class KeycloakDevContainer extends ExtendableKeycloakContainer<KeycloakDevContainer> {

    /**
     * Keycloak Docker image to use.
     */
    private static final String DEFAULT_IMAGE = "quay.io/keycloak/keycloak:26.5";

    public KeycloakDevContainer() {
        super(DEFAULT_IMAGE);
    }

    public void withFixedExposedPort(int hostPort, int containerPort) {
        super.addFixedExposedPort(hostPort, containerPort);
    }

    @Override
    protected void configure() {
        this.withExposedPorts(8080, 9000, 8443, 8787);
        this.withAdminUsername("admin");
        this.withAdminPassword("admin");
        this.withRealmImportFile("realm-export.json");
        this.withEnv("KC_HEALTH_ENABLED", "true");
        this.withEnv("KC_METRICS_ENABLED", "true");
        this.withDebugFixedPort(8787, true);

        super.configure();
        String[] commandParts = getCommandParts();
        String[] newCommandParts = Arrays.copyOf(commandParts, commandParts.length + 1);
        newCommandParts[newCommandParts.length - 1] = "--debug";
        setCommandParts(newCommandParts);

        String classesLocation = MountableFile.forClasspathResource(".").getResolvedPath()
                + "../../../kc-extension";
        this.createKeycloakExtensionProvider(classesLocation);
    }
}

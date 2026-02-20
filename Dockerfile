FROM quay.io/keycloak/keycloak:26.5

COPY ./src/test/resources/realm-export.json /opt/keycloak/data/import/realm-export.json
COPY ./build/libs/*.jar /opt/keycloak/providers/

ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start-dev", "--import-realm"]

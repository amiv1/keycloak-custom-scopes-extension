package com.example.keycloak;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.restassured.response.ValidatableResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScopeMappingTest extends KeycloakIntegrationTest {

    private static final String REALM = "test";

    @Test
    public void testServiceScopes() {
        ValidatableResponse response = given()
                .log().all()
                .param("client_id", "testclient")
                .param("client_secret", "testclient")
                .param("grant_type", "client_credentials")
                .param("scope", "s2s")
            .when()
                .post(getTokenUrl(REALM))
            .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK)
                .body("access_token", notNullValue())
                .body("token_type", equalTo("Bearer"));

        String accessToken = response.extract().path("access_token");
        DecodedJWT jwt = JWT.decode(accessToken);

        Claim scopeClaim = jwt.getClaim("scope");
        assertFalse(scopeClaim.isNull());

        List<String> scopes = scopeClaim.asList(String.class);
        assertTrue(scopes.contains("s2s"));
    }

    @Test
    public void testUserScopesAndUserInfo() throws Exception {
        // Step 1: Get access token for user
        ValidatableResponse tokenResponse = given()
                .log().all()
                .param("client_id", "testclient")
                .param("client_secret", "testclient")
                .param("username", "testuser")
                .param("password", "password123")
                .param("grant_type", "password")
                .param("scope", "openid")
            .when()
                .post(getTokenUrl(REALM))
            .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK)
                .body("access_token", notNullValue())
                .body("token_type", equalTo("Bearer"));

        String accessToken = tokenResponse.extract().path("access_token");

        DecodedJWT jwt = JWT.decode(accessToken);
        Claim scopeClaim = jwt.getClaim("scope");
        assertFalse(scopeClaim.isNull());

        List<String> scopes = scopeClaim.asList(String.class);
        assertTrue(scopes.contains("openid"));

        // Step 2: Use access token to fetch user info
        given()
                .log().all()
                .auth().oauth2(accessToken)
            .when()
                .get(getUserInfoUrl(REALM))
            .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK)
                .body("sub", notNullValue())
                .body("preferred_username", equalTo("testuser"));
    }
}

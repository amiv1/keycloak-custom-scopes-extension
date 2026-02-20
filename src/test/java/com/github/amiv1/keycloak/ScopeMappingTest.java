package com.github.amiv1.keycloak;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.restassured.response.ValidatableResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.keycloak.OAuth2Constants;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScopeMappingTest extends KeycloakIntegrationTest {

    private static final String REALM = "test";
    private static final String CLIENT_ID = "testclient";
    private static final String CLIENT_SECRET = "testclient";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "password123";

    @Test
    public void testServiceScopes() {
        ValidatableResponse response = given()
                .log().all()
                .param(OAuth2Constants.CLIENT_ID, CLIENT_ID)
                .param(OAuth2Constants.CLIENT_SECRET, CLIENT_SECRET)
                .param(OAuth2Constants.GRANT_TYPE, "client_credentials")
                .param(OAuth2Constants.SCOPE, "s2s")
            .when()
                .post(getTokenUrl(REALM))
            .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK)
                .body(OAuth2Constants.ACCESS_TOKEN, notNullValue())
                .body(OAuth2Constants.TOKEN_TYPE, equalTo("Bearer"));

        String accessToken = response.extract().path(OAuth2Constants.ACCESS_TOKEN);
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
                .param(OAuth2Constants.CLIENT_ID, CLIENT_ID)
                .param(OAuth2Constants.CLIENT_SECRET, CLIENT_SECRET)
                .param(OAuth2Constants.USERNAME, TEST_USERNAME)
                .param(OAuth2Constants.PASSWORD, TEST_PASSWORD)
                .param(OAuth2Constants.GRANT_TYPE, OAuth2Constants.PASSWORD)
                .param(OAuth2Constants.SCOPE, OAuth2Constants.SCOPE_OPENID)
            .when()
                .post(getTokenUrl(REALM))
            .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK)
                .body(OAuth2Constants.ACCESS_TOKEN, notNullValue())
                .body(OAuth2Constants.TOKEN_TYPE, equalTo("Bearer"));

        String accessToken = tokenResponse.extract().path(OAuth2Constants.ACCESS_TOKEN);

        DecodedJWT jwt = JWT.decode(accessToken);
        Claim scopeClaim = jwt.getClaim(OAuth2Constants.SCOPE);
        assertFalse(scopeClaim.isNull());

        List<String> scopes = scopeClaim.asList(String.class);
        assertTrue(scopes.contains(OAuth2Constants.SCOPE_OPENID));

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
                .body("preferred_username", equalTo(TEST_USERNAME));
    }

    @Test
    public void testTokenIntrospection() {
        // Step 1: Get access token
        ValidatableResponse tokenResponse = given()
                .log().all()
                .param(OAuth2Constants.CLIENT_ID, CLIENT_ID)
                .param(OAuth2Constants.CLIENT_SECRET, CLIENT_SECRET)
                .param(OAuth2Constants.GRANT_TYPE, "client_credentials")
                .param(OAuth2Constants.SCOPE, "s2s")
            .when()
                .post(getTokenUrl(REALM))
            .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK)
                .body(OAuth2Constants.ACCESS_TOKEN, notNullValue());

        String accessToken = tokenResponse.extract().path(OAuth2Constants.ACCESS_TOKEN);

        // Step 2: Introspect the token using client credentials as Basic auth
        given()
                .log().all()
                .param(OAuth2Constants.CLIENT_ID, CLIENT_ID)
                .param(OAuth2Constants.CLIENT_SECRET, CLIENT_SECRET)
                .param(OAuth2Constants.TOKEN, accessToken)
            .when()
                .post(getIntrospectUrl(REALM))
            .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK)
                .body("active", equalTo(true));
    }
}

package com.github.amiv1.keycloak.mapper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ArrayScopesProtocolMapper extends AbstractOIDCProtocolMapper
        implements OIDCAccessTokenMapper {

    public static final String PROVIDER_ID = "oidc-array-scopes-protocol-mapper";
    private static volatile boolean jsonModuleInjected;

    @Override
    public AccessToken transformAccessToken(AccessToken token, ProtocolMapperModel mappingModel,
                                            KeycloakSession session, UserSessionModel userSession,
                                            ClientSessionContext clientSessionCtx) {
        if (!jsonModuleInjected) {
            synchronized (this) {
                if (!jsonModuleInjected) {
                    injectAccessTokenDeserializer();
                }
            }
        }
        token.getOtherClaims().put("scope", Arrays.asList(token.getScope().split(" ")));
        setClaim(token, mappingModel, userSession, session, clientSessionCtx);
        return token;
    }

    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    @Override
    public String getDisplayType() {
        return "Array Scopes Mapper";
    }

    @Override
    public String getHelpText() {
        return "Modifies `scope` claim in resulting access token to be an array of strings "
                + "instead of a space-separated string.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /**
     * A workaround for the fact that {@link AccessToken#getScope()} is a String,
     * but we also want to accept an array of strings.
     * <p>
     * This deserializer will convert the "scope" claim from an array to a string during
     * deserialization, so that the rest of Keycloak can work with it as usual.
     * <p>
     * See the <a href="https://github.com/amiv1/keycloak-custom-scopes-extension/issues/1">issue</a>
     * in GitHub for more details.
     */
    private static void injectAccessTokenDeserializer() {
        ObjectMapper plainMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("access-token-scope-deserializer");
        module.addDeserializer(AccessToken.class, new JsonDeserializer<>() {
            @Override
            public AccessToken deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                // Read the whole token as a tree so we can inspect/modify the "scope" node
                ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
                ObjectNode root = mapper.readTree(jsonParser);

                JsonNode scopeNode = root.get("scope");
                if (scopeNode instanceof ArrayNode arrayNode) {
                    // Convert ["foo", "bar"] → "foo bar"
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < arrayNode.size(); i++) {
                        if (i > 0) sb.append(' ');
                        sb.append(arrayNode.get(i).asText());
                    }
                    root.put("scope", sb.toString());
                }
                // Deserialize using a plain ObjectMapper to avoid infinite recursion
                return plainMapper.treeToValue(root, AccessToken.class);
            }
        });
        JsonSerialization.mapper.registerModule(module);
        jsonModuleInjected = true;
    }
}

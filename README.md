# keycloak-custom-scopes-extension

This extension provides a custom mapper which overrides the default "scope" access token JWT claim.

The mapper takes the following "scope" value:
```json
"scope": "foo bar"
```

And transforms it into Array type:

```json
"scope": ["foo", "bar"]
```

## Run playground using Docker Compose

To run the playground, just execute the following command:

```shell
./gradlew jar
docker compose up --build
```

It will launch pre-configured Keycloak instance at http://localhost:8080 with the following client:

* Client ID: testclient
* Client Secret: testclient

You can use these credentials to authenticate using client credentials grant type and check scopes JWT claim
to verify that the mapper is working as expected.

```shell
curl --request POST --data 'grant_type=client_credentials&client_id=testclient&client_secret=testclient' http://localhost:8080/realms/test/protocol/openid-connect/token
```

<img width="591" alt="Screenshot 2023-07-13 at 12 21 10" src="https://github.com/gim-/keycloak-custom-scopes-extension/assets/1240012/0ecec47c-a7a3-4ea2-96fd-3728fc12788b">

## Building and installing extension to your own Keycloak instance

To build the extension, just run the following command:

```shell
./gradlew jar
```

Then copy resulting JAR file to your Keycloak's providers directory.
```shell
cp build/libs/keycloak-custom-scopes-extension-1.0.jar KEYCLOAK_ROOT/providers
```

Now you can run your Keycloak instance. The custom mapper should appear in available client mappers.

<img width="1211" alt="Screenshot 2023-07-13 at 12 24 16" src="https://github.com/gim-/keycloak-custom-scopes-extension/assets/1240012/917320ca-15b6-4360-9f7a-87536e185293">
<img width="1076" alt="Screenshot 2023-07-13 at 12 24 56" src="https://github.com/gim-/keycloak-custom-scopes-extension/assets/1240012/53a65f6a-1827-4592-97c2-28b0170951c2">
<img width="1182" alt="Screenshot 2023-07-13 at 12 25 08" src="https://github.com/gim-/keycloak-custom-scopes-extension/assets/1240012/25d6829a-6678-4054-aac8-2ee95da5e1d5">

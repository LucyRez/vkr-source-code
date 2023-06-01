package cs.hse.client.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;

@Entity
@Setter
@Getter
@EqualsAndHashCode
@NoArgsConstructor
public class Client {

    @SequenceGenerator(
            name = "client_sequence",
            sequenceName = "client_sequence",
            allocationSize = 1
    )
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "client_sequence"
    )
    private Long id;
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String scope;
    private String authMethod;
    private String grantType;

    public static Client from(RegisteredClient registeredClient) {
        Client client = new Client();
        client.setClientId(registeredClient.getClientId());
        client.setClientSecret(registeredClient.getClientSecret());
        client.setRedirectUri(registeredClient.getRedirectUris()
                .stream().findAny().get());
        client.setScope(
                registeredClient.getScopes().stream().findAny().get()
        );
        client.setAuthMethod(
                registeredClient.getClientAuthenticationMethods().stream().findAny().get().getValue()
        );
        client.setGrantType(
                registeredClient.getAuthorizationGrantTypes().stream().findAny().get().getValue()
        );

        return client;
    }

    public static RegisteredClient from(Client client) {
        return RegisteredClient.withId(String.valueOf(client.getId()))
                .clientId(client.getClientId())
                .clientSecret(client.getClientSecret())
                .scope(client.getScope())
                .redirectUri(client.getRedirectUri())
                .clientAuthenticationMethod(new ClientAuthenticationMethod(client.getAuthMethod()))
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofHours(12))
                        .refreshTokenTimeToLive(Duration.ofHours(24))
                        .build())
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(new AuthorizationGrantType(client.getGrantType()))
                .build();
    }
}

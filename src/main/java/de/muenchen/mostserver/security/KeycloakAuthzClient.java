package de.muenchen.mostserver.security;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.keycloak.authorization.client.AuthzClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class KeycloakAuthzClient {

    @Bean
    public HttpClient httpClient() {
        return HttpClientBuilder.create().build();
    }

    @Bean
    public AuthzClient keycloakClient(@Value("${spring.security.oauth2.client.registration.keycloak.client-secret}") String clientSecret, HttpClient httpClient) {
        var config = new org.keycloak.authorization.client.Configuration("http://localhost:8081/", "mostserver", "mostserver-client", Map.of("secret", clientSecret), httpClient);
        return AuthzClient.create(config);
    }
}

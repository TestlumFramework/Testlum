package com.testlum.testing.framework.auth;

import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.model.global_config.GoogleAuthStrategy;
import com.testlum.testing.model.scenario.Auth;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import com.testlum.testing.framework.util.JacksonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class GoogleAuthTest {

    private static final long EXPIRES_IN = 3600;

    private InterpreterDependencies dependencies;
    private OAuthCredentialsReader credentialsReader;
    private GoogleTokenClient tokenClient;
    private GoogleAuthStrategy configuration;
    private GoogleAuth strategy;
    private Auth command;

    @BeforeEach
    void setUp() {
        dependencies = mock(InterpreterDependencies.class);
        ApplicationContext context = mock(ApplicationContext.class);
        credentialsReader = mock(OAuthCredentialsReader.class);
        tokenClient = mock(GoogleTokenClient.class);
        when(dependencies.getContext()).thenReturn(context);
        when(context.getBean(OAuthCredentialsReader.class)).thenReturn(credentialsReader);
        when(context.getBean(GoogleTokenClient.class)).thenReturn(tokenClient);
        configuration = googleConfiguration();
        strategy = new GoogleAuth(dependencies, configuration);
        command = new Auth();
        command.setCredentials("google-user.json");
    }

    @Test
    void installsAccessTokenAsBearerWithoutExposingSecretsInMetadata() {
        successfulExchange("access-value");
        CommandResult result = new CommandResult();
        strategy.authenticate(command, result);
        var authorization = ArgumentCaptor.forClass(InterpreterDependencies.Authorization.class);
        verify(dependencies).setAuthorization(authorization.capture());
        assertEquals("Bearer access-value", authorization.getValue().getHeaders().get("Authorization"));
        assertEquals("access-value", strategy.getAccessToken().orElseThrow());
        assertEquals("GOOGLE", result.getMetadata().get("Authentication type"));
        assertFalse(result.getMetadata().toString().contains("access-value"));
        assertFalse(result.getMetadata().toString().contains("refresh-value"));
        assertFalse(result.getMetadata().toString().contains(configuration.getClientSecret()));
        verify(credentialsReader).readRefreshToken("google-user.json");
        verify(tokenClient).refreshToken(configuration, "refresh-value");
    }

    @Test
    void eachAuthenticationPerformsANewTokenExchange() {
        when(credentialsReader.readRefreshToken("google-user.json")).thenReturn("refresh-value");
        when(tokenClient.refreshToken(configuration, "refresh-value"))
                .thenReturn(response("first-access"), response("second-access"));
        strategy.authenticate(command, new CommandResult());
        strategy.authenticate(command, new CommandResult());
        var authorization = ArgumentCaptor.forClass(InterpreterDependencies.Authorization.class);
        verify(dependencies, times(2)).setAuthorization(authorization.capture());
        assertEquals("Bearer first-access", authorization.getAllValues().get(0).getHeaders().get("Authorization"));
        assertEquals("Bearer second-access", authorization.getAllValues().get(1).getHeaders().get("Authorization"));
        verify(credentialsReader, times(2)).readRefreshToken("google-user.json");
        verify(tokenClient, times(2)).refreshToken(configuration, "refresh-value");
    }

    @Test
    void credentialsFailurePreventsTokenRequestAndAuthorization() {
        DefaultFrameworkException failure = new DefaultFrameworkException("Invalid Google credentials");
        when(credentialsReader.readRefreshToken("google-user.json")).thenThrow(failure);
        assertSame(failure, assertThrows(DefaultFrameworkException.class,
                () -> strategy.authenticate(command, new CommandResult())));
        verifyNoInteractions(tokenClient);
        verify(dependencies, never()).setAuthorization(any());
    }

    @Test
    void tokenFailurePreventsAuthorization() {
        when(credentialsReader.readRefreshToken("google-user.json")).thenReturn("refresh-value");
        DefaultFrameworkException failure = new DefaultFrameworkException("Google token request failed: invalid_grant");
        when(tokenClient.refreshToken(configuration, "refresh-value")).thenThrow(failure);
        assertSame(failure, assertThrows(DefaultFrameworkException.class,
                () -> strategy.authenticate(command, new CommandResult())));
        verify(dependencies, never()).setAuthorization(any());
    }

    @Test
    void logoutOnlyClearsLocalAuthorization() {
        strategy.logout();
        verify(dependencies).clearAuthorization();
        verifyNoInteractions(credentialsReader, tokenClient);
    }

    @Test
    void tokenClientSendsGoogleRefreshFormWithoutMicrosoftScope() {
        var client = mock(RestClient.class,
                RETURNS_DEEP_STUBS);
        String endpoint = "https://oauth2.googleapis.com/token";
        var request = client.post().uri(endpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON);
        when(request.body(any(MultiValueMap.class)).retrieve().body(String.class))
                .thenReturn("{\"access_token\":\"access-value\",\"token_type\":\"Bearer\",\"expires_in\":3600}");
        clearInvocations(request);

        final OAuthTokenResponse response = new GoogleTokenClient(
                new JacksonService(), client)
                .refreshToken(configuration, "refresh-value");

        var form = ArgumentCaptor.forClass(MultiValueMap.class);
        verify(request).body(form.capture());
        assertEquals("refresh_token", form.getValue().getFirst("grant_type"));
        assertEquals("client-id", form.getValue().getFirst("client_id"));
        assertEquals("client-secret", form.getValue().getFirst("client_secret"));
        assertEquals("refresh-value", form.getValue().getFirst("refresh_token"));
        assertFalse(form.getValue().containsKey("scope"));
        assertEquals("access-value", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(EXPIRES_IN, response.getExpiresIn());
    }

    private GoogleAuthStrategy googleConfiguration() {
        GoogleAuthStrategy google = new GoogleAuthStrategy();
        google.setAlias("GOOGLE");
        google.setClientId("client-id");
        google.setClientSecret("client-secret");
        return google;
    }

    private void successfulExchange(final String accessToken) {
        when(credentialsReader.readRefreshToken("google-user.json")).thenReturn("refresh-value");
        when(tokenClient.refreshToken(configuration, "refresh-value")).thenReturn(response(accessToken));
    }

    private OAuthTokenResponse response(final String accessToken) {
        return new OAuthTokenResponse(accessToken, "Bearer", EXPIRES_IN);
    }
}

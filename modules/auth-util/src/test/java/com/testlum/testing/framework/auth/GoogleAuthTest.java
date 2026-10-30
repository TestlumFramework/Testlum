package com.testlum.testing.framework.auth;

import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.model.global_config.GoogleAuthStrategy;
import com.testlum.testing.model.scenario.Auth;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class GoogleAuthTest {

    private static final long EXPIRES_IN = 3600;

    private InterpreterDependencies dependencies;
    private GoogleCredentialsReader credentialsReader;
    private GoogleTokenClient tokenClient;
    private GoogleAuthStrategy configuration;
    private GoogleAuth strategy;
    private Auth command;

    @BeforeEach
    void setUp() {
        dependencies = mock(InterpreterDependencies.class);
        ApplicationContext context = mock(ApplicationContext.class);
        credentialsReader = mock(GoogleCredentialsReader.class);
        tokenClient = mock(GoogleTokenClient.class);
        when(dependencies.getContext()).thenReturn(context);
        when(context.getBean(GoogleCredentialsReader.class)).thenReturn(credentialsReader);
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

    private GoogleTokenResponse response(final String accessToken) {
        return new GoogleTokenResponse(accessToken, "Bearer", EXPIRES_IN);
    }
}

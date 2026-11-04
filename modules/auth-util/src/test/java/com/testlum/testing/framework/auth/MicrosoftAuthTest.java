package com.testlum.testing.framework.auth;

import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.model.global_config.MicrosoftAuthStrategy;
import com.testlum.testing.model.scenario.Auth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.io.TempDir;
import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.util.JacksonService;
import com.testlum.testing.framework.util.SystemVariableService;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
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
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.ArgumentMatchers.anyString;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MicrosoftAuthTest {

    private static final long EXPIRES_IN = 3600;

    private InterpreterDependencies dependencies;
    private OAuthCredentialsReader credentialsReader;
    private MicrosoftTokenClient tokenClient;
    private MicrosoftAuthStrategy configuration;
    private MicrosoftAuth strategy;
    private Auth command;

    @BeforeEach
    void setUp() {
        dependencies = mock(InterpreterDependencies.class);
        ApplicationContext context = mock(ApplicationContext.class);
        credentialsReader = mock(OAuthCredentialsReader.class);
        tokenClient = mock(MicrosoftTokenClient.class);
        when(dependencies.getContext()).thenReturn(context);
        when(context.getBean(OAuthCredentialsReader.class)).thenReturn(credentialsReader);
        when(context.getBean(MicrosoftTokenClient.class)).thenReturn(tokenClient);
        configuration = microsoftConfiguration();
        strategy = new MicrosoftAuth(dependencies, configuration);
        command = new Auth();
        command.setCredentials("microsoft-user.json");
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
        assertEquals("MICROSOFT", result.getMetadata().get("Authentication type"));
        assertFalse(result.getMetadata().toString().contains("access-value"));
        assertFalse(result.getMetadata().toString().contains("refresh-value"));
        assertFalse(result.getMetadata().toString().contains(configuration.getClientSecret()));
        verify(credentialsReader).readRefreshToken("microsoft-user.json");
        verify(tokenClient).refreshToken(configuration, "refresh-value");
    }

    @Test
    void eachAuthenticationPerformsANewTokenExchange() {
        when(credentialsReader.readRefreshToken("microsoft-user.json")).thenReturn("refresh-value");
        when(tokenClient.refreshToken(configuration, "refresh-value"))
                .thenReturn(response("first-access"), response("second-access"));
        strategy.authenticate(command, new CommandResult());
        strategy.authenticate(command, new CommandResult());
        var authorization = ArgumentCaptor.forClass(InterpreterDependencies.Authorization.class);
        verify(dependencies, times(2)).setAuthorization(authorization.capture());
        assertEquals("Bearer first-access", authorization.getAllValues().get(0).getHeaders().get("Authorization"));
        assertEquals("Bearer second-access", authorization.getAllValues().get(1).getHeaders().get("Authorization"));
        verify(credentialsReader, times(2)).readRefreshToken("microsoft-user.json");
        verify(tokenClient, times(2)).refreshToken(configuration, "refresh-value");
    }

    @Test
    void credentialsFailurePreventsTokenRequestAndAuthorization() {
        DefaultFrameworkException failure = new DefaultFrameworkException("Invalid Microsoft credentials");
        when(credentialsReader.readRefreshToken("microsoft-user.json")).thenThrow(failure);
        assertSame(failure, assertThrows(DefaultFrameworkException.class,
                () -> strategy.authenticate(command, new CommandResult())));
        verifyNoInteractions(tokenClient);
        verify(dependencies, never()).setAuthorization(any());
    }

    @Test
    void tokenFailurePreventsAuthorization() {
        when(credentialsReader.readRefreshToken("microsoft-user.json")).thenReturn("refresh-value");
        DefaultFrameworkException failure =
                new DefaultFrameworkException("Microsoft token request failed: invalid_grant");
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

    private MicrosoftAuthStrategy microsoftConfiguration() {
        MicrosoftAuthStrategy microsoft = new MicrosoftAuthStrategy();
        microsoft.setAlias("MICROSOFT");
        microsoft.setClientId("client-id");
        microsoft.setClientSecret("client-secret");
        microsoft.setTenant("common");
        microsoft.setScope("https://graph.microsoft.com/User.Read");
        return microsoft;
    }

    private void successfulExchange(final String accessToken) {
        when(credentialsReader.readRefreshToken("microsoft-user.json")).thenReturn("refresh-value");
        when(tokenClient.refreshToken(configuration, "refresh-value")).thenReturn(response(accessToken));
    }

    private OAuthTokenResponse response(final String accessToken) {
        return new OAuthTokenResponse(accessToken, "Bearer", EXPIRES_IN);
    }

    @Nested
    class Credentials {
        @TempDir
        Path directory;

        @Test
        void resolvesRefreshTokenFromEnvironmentWithoutRewritingCredentials() throws IOException {
            String json = "{\"refreshToken\":\"${PATH}\"}";
            Path file = Files.writeString(directory.resolve("credentials.json"), json);
            OAuthCredentialsReader reader = reader(file);
            assertEquals(System.getenv("PATH"), reader.readRefreshToken("credentials.json"));
            assertEquals(json, Files.readString(file));
        }

        @Test
        void rejectsMissingBlankAndNonStringTokensWithoutExposingPayload() throws IOException {
            for (String json : java.util.List.of("[]", "{}", "{\"refreshToken\":null}",
                    "{\"refreshToken\":123}", "{\"refreshToken\":\" \"}", "invalid-sensitive-payload")) {
                Path file = Files.writeString(directory.resolve("credentials.json"), json);
                DefaultFrameworkException error = assertThrows(DefaultFrameworkException.class,
                        () -> reader(file).readRefreshToken("credentials.json"));
                assertFalse(error.getMessage().contains("sensitive-payload"));
                assertNull(error.getCause());
            }
        }

        @Test
        void rejectsMissingFileAndMissingEnvironmentVariable() throws IOException {
            assertThrows(DefaultFrameworkException.class,
                    () -> reader(directory.resolve("absent.json")).readRefreshToken("absent.json"));
            Path file = Files.writeString(directory.resolve("credentials.json"),
                    "{\"refreshToken\":\"${THIS_VAR_SHOULD_NOT_EXIST_12345}\"}");
            assertThrows(DefaultFrameworkException.class, () -> reader(file).readRefreshToken("credentials.json"));
        }

        private OAuthCredentialsReader reader(final Path file) {
            FileSearcher searcher = mock(FileSearcher.class);
            when(searcher.searchFileFromDataFolder(anyString())).thenReturn(file.toFile());
            return new OAuthCredentialsReader(searcher, new SystemVariableService(), new JacksonService());
        }
    }

    @Nested
    class TokenExchange {
        private static final String ENDPOINT = "https://login.microsoftonline.com/common/oauth2/v2.0/token";
        private static final String VALID_RESPONSE =
                "{\"access_token\":\"access-secret\",\"token_type\":\"Bearer\",\"expires_in\":3600,"
                        + "\"refresh_token\":\"new-refresh-secret\"}";

        @Test
        void sendsRefreshGrantWithScopeAndReturnsExportableBearerToken() {
            RestClient client = clientWithResponse(VALID_RESPONSE);
            OAuthTokenResponse result = new MicrosoftTokenClient(new JacksonService(), client)
                    .refreshToken(configuration, "refresh-secret");
            assertEquals("access-secret", result.getAccessToken());
            assertEquals("Bearer", result.getTokenType());
            assertEquals(EXPIRES_IN, result.getExpiresIn());
            ArgumentCaptor<MultiValueMap> form = ArgumentCaptor.forClass(MultiValueMap.class);
            verify(client.post().uri(ENDPOINT).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)).body(form.capture());
            assertEquals("refresh_token", form.getValue().getFirst("grant_type"));
            assertEquals("client-id", form.getValue().getFirst("client_id"));
            assertEquals("client-secret", form.getValue().getFirst("client_secret"));
            assertEquals("refresh-secret", form.getValue().getFirst("refresh_token"));
            assertEquals("https://graph.microsoft.com/User.Read", form.getValue().getFirst("scope"));
            assertFalse(result.toString().contains("access-secret"));
        }

        @Test
        void rejectsMalformedOrIncompleteResponsesWithoutLeakingSecrets() {
            for (String json : java.util.List.of("", "[]", "invalid-sensitive-payload", "{}",
                    VALID_RESPONSE.replace("Bearer", "DPoP"), VALID_RESPONSE.replace("3600", "0"),
                    VALID_RESPONSE.replace("3600", "\"3600\""), VALID_RESPONSE.replace("access-secret", " "))) {
                MicrosoftTokenClient client = new MicrosoftTokenClient(new JacksonService(), clientWithResponse(json));
                DefaultFrameworkException error = assertThrows(DefaultFrameworkException.class,
                        () -> client.refreshToken(configuration, "refresh-secret"));
                assertFalse(error.getMessage().contains("sensitive-payload"));
                assertFalse(error.getMessage().contains("access-secret"));
                assertNull(error.getCause());
            }
        }

        @Test
        void sanitizesProviderAndTransportErrors() {
            RestClient client = mock(RestClient.class, RETURNS_DEEP_STUBS);
            when(client.post().uri(ENDPOINT).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON).body(any(MultiValueMap.class)).retrieve().body(String.class))
                    .thenThrow(new RestClientResponseException("sensitive-payload", 400, "Bad Request",
                            new HttpHeaders(), ("{\"error\":\"invalid_grant\","
                                    + "\"error_description\":\"sensitive-payload\"}")
                            .getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
            DefaultFrameworkException error = assertThrows(DefaultFrameworkException.class,
                    () -> new MicrosoftTokenClient(new JacksonService(), client)
                            .refreshToken(configuration, "refresh-secret"));
            assertFalse(error.getMessage().contains("sensitive-payload"));
            assertTrue(error.getMessage().contains("invalid_grant"));
            assertNull(error.getCause());
        }

        @Test
        void sanitizesTransportFailure() {
            RestClient client = mock(RestClient.class, RETURNS_DEEP_STUBS);
            when(client.post().uri(ENDPOINT).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON).body(any(MultiValueMap.class)).retrieve().body(String.class))
                    .thenThrow(new RestClientException("sensitive-transport-details"));
            DefaultFrameworkException error = assertThrows(DefaultFrameworkException.class,
                    () -> new MicrosoftTokenClient(new JacksonService(), client)
                            .refreshToken(configuration, "refresh-secret"));
            assertFalse(error.getMessage().contains("sensitive-transport-details"));
            assertNull(error.getCause());
        }

        @Test
        void rejectsInvalidConfigurationBeforeSendingCredentials() {
            MicrosoftTokenClient client = new MicrosoftTokenClient(new JacksonService(), mock(RestClient.class));
            configuration.setTenant("common/other");
            assertThrows(DefaultFrameworkException.class, () -> client.refreshToken(configuration, "refresh-secret"));
            configuration.setTenant("common");
            configuration.setScope(" ");
            assertThrows(DefaultFrameworkException.class, () -> client.refreshToken(configuration, "refresh-secret"));
            assertThrows(DefaultFrameworkException.class, () -> client.refreshToken(null, "refresh-secret"));
        }

        private RestClient clientWithResponse(final String json) {
            RestClient client = mock(RestClient.class, RETURNS_DEEP_STUBS);
            when(client.post().uri(ENDPOINT).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON).body(any(MultiValueMap.class)).retrieve().body(String.class))
                    .thenReturn(json);
            clearInvocations(client.post().uri(ENDPOINT).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON));
            return client;
        }
    }
}

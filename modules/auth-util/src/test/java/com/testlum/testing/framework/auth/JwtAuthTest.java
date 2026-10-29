package com.testlum.testing.framework.auth;

import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.util.SystemVariableService;
import com.testlum.testing.model.global_config.JwtAuthStrategy;
import com.testlum.testing.model.scenario.Auth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClient;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthTest {

    @TempDir
    Path tempDir;

    private InterpreterDependencies dependencies;
    private JwtAuthStrategy configuration;
    private FileSearcher fileSearcher;
    private SystemVariableService systemVariableService;
    private JwtAuth jwtAuth;

    @BeforeEach
    void setUp() {
        dependencies = mock(InterpreterDependencies.class);
        fileSearcher = mock(FileSearcher.class);
        systemVariableService = mock(SystemVariableService.class);

        ApplicationContext context = mock(ApplicationContext.class);
        when(dependencies.getContext()).thenReturn(context);
        when(context.getBean(FileSearcher.class)).thenReturn(fileSearcher);
        when(context.getBean(SystemVariableService.class)).thenReturn(systemVariableService);
        lenient().when(systemVariableService.inject(anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        configuration = new JwtAuthStrategy();
        configuration.setAlias("JWT");
        configuration.setLoginEndpoint("http://localhost:8080/auth/login");
        jwtAuth = new JwtAuth(dependencies, configuration);
    }

    @Nested
    class Authenticate {

        @Test
        void setsJwtAuthenticationTypeOnResult() throws IOException {
            File credFile = createCredentialsFile("{\"username\":\"admin\",\"password\":\"secret\"}");
            when(fileSearcher.searchFileFromDataFolder("creds.json")).thenReturn(credFile);

            Auth auth = buildAuth("myApi", "creds.json");
            CommandResult result = new CommandResult();

            RestClient restClient = mock(RestClient.class);
            RestClient.RequestBodyUriSpec bodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
            RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

            try (MockedStatic<RestClient> mockedRestClient = mockStatic(RestClient.class)) {
                mockedRestClient.when(RestClient::create).thenReturn(restClient);
                when(restClient.post()).thenReturn(bodyUriSpec);
                when(bodyUriSpec.uri(configuration.getLoginEndpoint())).thenReturn(bodySpec);
                when(bodySpec.headers(any())).thenReturn(bodySpec);
                when(bodySpec.body(anyString())).thenReturn(bodySpec);
                when(bodySpec.retrieve()).thenReturn(responseSpec);
                when(responseSpec.body(String.class)).thenReturn("{\"token\":\"jwt-abc-123\"}");

                jwtAuth.authenticate(auth, result);
            }

            assertEquals(AuthorizationConstant.HEADER_JWT,
                    result.getMetadata().get("Authentication type"));
        }

        @Test
        void setsAuthorizationHeaderWithBearerToken() throws IOException {
            File credFile = createCredentialsFile("{\"username\":\"admin\",\"password\":\"secret\"}");
            when(fileSearcher.searchFileFromDataFolder("creds.json")).thenReturn(credFile);


            Auth auth = buildAuth("myApi", "creds.json");
            CommandResult result = new CommandResult();

            RestClient restClient = mock(RestClient.class);
            RestClient.RequestBodyUriSpec bodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
            RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

            try (MockedStatic<RestClient> mockedRestClient = mockStatic(RestClient.class)) {
                mockedRestClient.when(RestClient::create).thenReturn(restClient);
                when(restClient.post()).thenReturn(bodyUriSpec);
                when(bodyUriSpec.uri(configuration.getLoginEndpoint())).thenReturn(bodySpec);
                when(bodySpec.headers(any())).thenReturn(bodySpec);
                when(bodySpec.body(anyString())).thenReturn(bodySpec);
                when(bodySpec.retrieve()).thenReturn(responseSpec);
                when(responseSpec.body(String.class)).thenReturn("{\"token\":\"jwt-abc-123\"}");

                jwtAuth.authenticate(auth, result);
            }

            ArgumentCaptor<InterpreterDependencies.Authorization> captor =
                    ArgumentCaptor.forClass(InterpreterDependencies.Authorization.class);
            verify(dependencies).setAuthorization(captor.capture());

            String headerValue = captor.getValue().getHeaders()
                    .get(AuthorizationConstant.HEADER_AUTHORIZATION);
            assertEquals("Bearer jwt-abc-123", headerValue);
        }

        @Test
        void supportsExplicitJsonPath() throws IOException {
            File credFile = createCredentialsFile("{\"username\":\"admin\",\"password\":\"secret\"}");
            when(fileSearcher.searchFileFromDataFolder("creds.json")).thenReturn(credFile);


            Auth auth = buildAuth("myApi", "creds.json");
            CommandResult result = new CommandResult();
            configuration.setTokenName("$.data.accessToken");

            RestClient restClient = mock(RestClient.class);
            RestClient.RequestBodyUriSpec bodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
            RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

            try (MockedStatic<RestClient> mockedRestClient = mockStatic(RestClient.class)) {
                mockedRestClient.when(RestClient::create).thenReturn(restClient);
                when(restClient.post()).thenReturn(bodyUriSpec);
                when(bodyUriSpec.uri(configuration.getLoginEndpoint())).thenReturn(bodySpec);
                when(bodySpec.headers(any())).thenReturn(bodySpec);
                when(bodySpec.body(anyString())).thenReturn(bodySpec);
                when(bodySpec.retrieve()).thenReturn(responseSpec);
                when(responseSpec.body(String.class)).thenReturn("{\"data\":{\"accessToken\":\"jwt-abc-123\"}}");

                jwtAuth.authenticate(auth, result);
            }

            ArgumentCaptor<InterpreterDependencies.Authorization> captor =
                    ArgumentCaptor.forClass(InterpreterDependencies.Authorization.class);
            verify(dependencies).setAuthorization(captor.capture());

            String headerValue = captor.getValue().getHeaders()
                    .get(AuthorizationConstant.HEADER_AUTHORIZATION);
            assertEquals("Bearer jwt-abc-123", headerValue);
        }

        @Test
        void rejectsEmptyToken() throws IOException {
            File credFile = createCredentialsFile("{\"username\":\"admin\",\"password\":\"secret\"}");
            when(fileSearcher.searchFileFromDataFolder("creds.json")).thenReturn(credFile);


            Auth auth = buildAuth("myApi", "creds.json");
            CommandResult result = new CommandResult();

            RestClient restClient = mock(RestClient.class);
            RestClient.RequestBodyUriSpec bodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
            RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

            try (MockedStatic<RestClient> mockedRestClient = mockStatic(RestClient.class)) {
                mockedRestClient.when(RestClient::create).thenReturn(restClient);
                when(restClient.post()).thenReturn(bodyUriSpec);
                when(bodyUriSpec.uri(configuration.getLoginEndpoint())).thenReturn(bodySpec);
                when(bodySpec.headers(any())).thenReturn(bodySpec);
                when(bodySpec.body(anyString())).thenReturn(bodySpec);
                when(bodySpec.retrieve()).thenReturn(responseSpec);
                when(responseSpec.body(String.class)).thenReturn("{\"token\":\"\"}");

                assertThrows(DefaultFrameworkException.class, () -> jwtAuth.authenticate(auth, result));
            }

            verify(dependencies, never()).setAuthorization(any());
        }

        @Test
        void rejectsNonStringToken() throws IOException {
            File credFile = createCredentialsFile("{\"username\":\"admin\",\"password\":\"secret\"}");
            when(fileSearcher.searchFileFromDataFolder("creds.json")).thenReturn(credFile);


            Auth auth = buildAuth("myApi", "creds.json");
            CommandResult result = new CommandResult();

            RestClient restClient = mock(RestClient.class);
            RestClient.RequestBodyUriSpec bodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
            RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

            try (MockedStatic<RestClient> mockedRestClient = mockStatic(RestClient.class)) {
                mockedRestClient.when(RestClient::create).thenReturn(restClient);
                when(restClient.post()).thenReturn(bodyUriSpec);
                when(bodyUriSpec.uri(configuration.getLoginEndpoint())).thenReturn(bodySpec);
                when(bodySpec.headers(any())).thenReturn(bodySpec);
                when(bodySpec.body(anyString())).thenReturn(bodySpec);
                when(bodySpec.retrieve()).thenReturn(responseSpec);
                when(responseSpec.body(String.class)).thenReturn("{\"token\":123}");

                assertThrows(DefaultFrameworkException.class, () -> jwtAuth.authenticate(auth, result));
            }

            verify(dependencies, never()).setAuthorization(any());
        }

        @Test
        void rejectsMissingToken() throws IOException {
            File credFile = createCredentialsFile("{\"username\":\"admin\",\"password\":\"secret\"}");
            when(fileSearcher.searchFileFromDataFolder("creds.json")).thenReturn(credFile);


            Auth auth = buildAuth("myApi", "creds.json");
            CommandResult result = new CommandResult();

            RestClient restClient = mock(RestClient.class);
            RestClient.RequestBodyUriSpec bodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
            RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

            try (MockedStatic<RestClient> mockedRestClient = mockStatic(RestClient.class)) {
                mockedRestClient.when(RestClient::create).thenReturn(restClient);
                when(restClient.post()).thenReturn(bodyUriSpec);
                when(bodyUriSpec.uri(configuration.getLoginEndpoint())).thenReturn(bodySpec);
                when(bodySpec.headers(any())).thenReturn(bodySpec);
                when(bodySpec.body(anyString())).thenReturn(bodySpec);
                when(bodySpec.retrieve()).thenReturn(responseSpec);
                when(responseSpec.body(String.class)).thenReturn("{}");

                assertThrows(com.jayway.jsonpath.PathNotFoundException.class, () -> jwtAuth.authenticate(auth, result));
            }

            verify(dependencies, never()).setAuthorization(any());
        }

        @Test
        void usesCustomTokenNameFromStrategyConfig() throws IOException {
            File credFile = createCredentialsFile("{\"username\":\"admin\",\"password\":\"secret\"}");
            when(fileSearcher.searchFileFromDataFolder("creds.json")).thenReturn(credFile);

            configuration.setTokenName("access_token");

            Auth auth = buildAuth("myApi", "creds.json");
            CommandResult result = new CommandResult();

            RestClient restClient = mock(RestClient.class);
            RestClient.RequestBodyUriSpec bodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
            RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

            try (MockedStatic<RestClient> mockedRestClient = mockStatic(RestClient.class)) {
                mockedRestClient.when(RestClient::create).thenReturn(restClient);
                when(restClient.post()).thenReturn(bodyUriSpec);
                when(bodyUriSpec.uri(configuration.getLoginEndpoint())).thenReturn(bodySpec);
                when(bodySpec.headers(any())).thenReturn(bodySpec);
                when(bodySpec.body(anyString())).thenReturn(bodySpec);
                when(bodySpec.retrieve()).thenReturn(responseSpec);
                when(responseSpec.body(String.class))
                        .thenReturn("{\"access_token\":\"custom-token-456\"}");

                jwtAuth.authenticate(auth, result);
            }

            ArgumentCaptor<InterpreterDependencies.Authorization> captor =
                    ArgumentCaptor.forClass(InterpreterDependencies.Authorization.class);
            verify(dependencies).setAuthorization(captor.capture());

            String headerValue = captor.getValue().getHeaders()
                    .get(AuthorizationConstant.HEADER_AUTHORIZATION);
            assertEquals("Bearer custom-token-456", headerValue);
        }

        @Test
        void throwsWhenResponseIsBlank() throws IOException {
            File credFile = createCredentialsFile("{\"username\":\"admin\",\"password\":\"secret\"}");
            when(fileSearcher.searchFileFromDataFolder("creds.json")).thenReturn(credFile);


            Auth auth = buildAuth("myApi", "creds.json");
            CommandResult result = new CommandResult();

            RestClient restClient = mock(RestClient.class);
            RestClient.RequestBodyUriSpec bodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
            RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

            try (MockedStatic<RestClient> mockedRestClient = mockStatic(RestClient.class)) {
                mockedRestClient.when(RestClient::create).thenReturn(restClient);
                when(restClient.post()).thenReturn(bodyUriSpec);
                when(bodyUriSpec.uri(configuration.getLoginEndpoint())).thenReturn(bodySpec);
                when(bodySpec.headers(any())).thenReturn(bodySpec);
                when(bodySpec.body(anyString())).thenReturn(bodySpec);
                when(bodySpec.retrieve()).thenReturn(responseSpec);
                when(responseSpec.body(String.class)).thenReturn("");

                assertThrows(DefaultFrameworkException.class,
                        () -> jwtAuth.authenticate(auth, result));
            }

            verify(dependencies, never()).setAuthorization(any());
        }

        @Test
        void throwsWhenResponseIsNull() throws IOException {
            File credFile = createCredentialsFile("{\"username\":\"admin\",\"password\":\"secret\"}");
            when(fileSearcher.searchFileFromDataFolder("creds.json")).thenReturn(credFile);


            Auth auth = buildAuth("myApi", "creds.json");
            CommandResult result = new CommandResult();

            RestClient restClient = mock(RestClient.class);
            RestClient.RequestBodyUriSpec bodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
            RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

            try (MockedStatic<RestClient> mockedRestClient = mockStatic(RestClient.class)) {
                mockedRestClient.when(RestClient::create).thenReturn(restClient);
                when(restClient.post()).thenReturn(bodyUriSpec);
                when(bodyUriSpec.uri(configuration.getLoginEndpoint())).thenReturn(bodySpec);
                when(bodySpec.headers(any())).thenReturn(bodySpec);
                when(bodySpec.body(anyString())).thenReturn(bodySpec);
                when(bodySpec.retrieve()).thenReturn(responseSpec);
                when(responseSpec.body(String.class)).thenReturn(null);

                assertThrows(DefaultFrameworkException.class,
                        () -> jwtAuth.authenticate(auth, result));
            }

            verify(dependencies, never()).setAuthorization(any());
        }
    }

    @Nested
    class DoRequestErrorHandling {

        @Test
        void propagatesNotFoundLoginFailure() throws IOException {
            File credFile = createCredentialsFile("{\"username\":\"admin\",\"password\":\"secret\"}");
            when(fileSearcher.searchFileFromDataFolder("creds.json")).thenReturn(credFile);


            Auth auth = buildAuth("myApi", "creds.json");
            CommandResult result = new CommandResult();

            RestClient restClient = mock(RestClient.class);
            RestClient.RequestBodyUriSpec bodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);

            try (MockedStatic<RestClient> mockedRestClient = mockStatic(RestClient.class)) {
                mockedRestClient.when(RestClient::create).thenReturn(restClient);
                when(restClient.post()).thenReturn(bodyUriSpec);
                when(bodyUriSpec.uri(configuration.getLoginEndpoint())).thenReturn(bodySpec);
                when(bodySpec.headers(any())).thenReturn(bodySpec);
                when(bodySpec.body(anyString())).thenReturn(bodySpec);
                when(bodySpec.retrieve()).thenThrow(
                        new HttpClientErrorException(HttpStatus.NOT_FOUND));

                assertThrows(DefaultFrameworkException.class,
                        () -> jwtAuth.authenticate(auth, result));
            }

            verify(dependencies, never()).setAuthorization(any());
        }

        @Test
        void propagatesBadGatewayLoginFailure() throws IOException {
            File credFile = createCredentialsFile("{\"username\":\"admin\",\"password\":\"secret\"}");
            when(fileSearcher.searchFileFromDataFolder("creds.json")).thenReturn(credFile);


            Auth auth = buildAuth("myApi", "creds.json");
            CommandResult result = new CommandResult();

            RestClient restClient = mock(RestClient.class);
            RestClient.RequestBodyUriSpec bodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);

            try (MockedStatic<RestClient> mockedRestClient = mockStatic(RestClient.class)) {
                mockedRestClient.when(RestClient::create).thenReturn(restClient);
                when(restClient.post()).thenReturn(bodyUriSpec);
                when(bodyUriSpec.uri(configuration.getLoginEndpoint())).thenReturn(bodySpec);
                when(bodySpec.headers(any())).thenReturn(bodySpec);
                when(bodySpec.body(anyString())).thenReturn(bodySpec);
                when(bodySpec.retrieve()).thenThrow(
                        new HttpServerErrorException(HttpStatus.BAD_GATEWAY));

                assertThrows(DefaultFrameworkException.class,
                        () -> jwtAuth.authenticate(auth, result));
            }

            verify(dependencies, never()).setAuthorization(any());
        }

        @Test
        void propagatesForbiddenLoginFailure() throws IOException {
            File credFile = createCredentialsFile("{\"username\":\"admin\",\"password\":\"secret\"}");
            when(fileSearcher.searchFileFromDataFolder("creds.json")).thenReturn(credFile);


            Auth auth = buildAuth("myApi", "creds.json");
            CommandResult result = new CommandResult();

            RestClient restClient = mock(RestClient.class);
            RestClient.RequestBodyUriSpec bodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);

            try (MockedStatic<RestClient> mockedRestClient = mockStatic(RestClient.class)) {
                mockedRestClient.when(RestClient::create).thenReturn(restClient);
                when(restClient.post()).thenReturn(bodyUriSpec);
                when(bodyUriSpec.uri(configuration.getLoginEndpoint())).thenReturn(bodySpec);
                when(bodySpec.headers(any())).thenReturn(bodySpec);
                when(bodySpec.body(anyString())).thenReturn(bodySpec);
                when(bodySpec.retrieve()).thenThrow(
                        new HttpClientErrorException(HttpStatus.FORBIDDEN));

                assertThrows(DefaultFrameworkException.class,
                        () -> jwtAuth.authenticate(auth, result));
            }

            verify(dependencies, never()).setAuthorization(any());
        }
    }

    @Nested
    class GetCredentialsFromFile {

        @Test
        void throwsDefaultFrameworkExceptionOnIOException() {
            when(fileSearcher.searchFileFromDataFolder("missing.json"))
                    .thenReturn(new File("/nonexistent/path/missing.json"));

            Auth auth = buildAuth("myApi", "missing.json");
            CommandResult result = new CommandResult();

            assertThrows(DefaultFrameworkException.class,
                    () -> jwtAuth.authenticate(auth, result));
        }
    }

    @Nested
    class EnvironmentVariableInjection {

        @Test
        void sendsRequestBodyWithResolvedEnvVariables() throws IOException {
            String rawBody = "{\"username\":\"${API_USER}\",\"password\":\"${API_PASS}\"}";
            String injectedBody = "{\"username\":\"admin\",\"password\":\"secret\"}";
            File credFile = createCredentialsFile(rawBody);
            when(fileSearcher.searchFileFromDataFolder("env-creds.json")).thenReturn(credFile);
            when(systemVariableService.inject(rawBody)).thenReturn(injectedBody);


            Auth auth = buildAuth("myApi", "env-creds.json");
            CommandResult result = new CommandResult();

            RestClient restClient = mock(RestClient.class);
            RestClient.RequestBodyUriSpec bodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
            RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

            try (MockedStatic<RestClient> mockedRestClient = mockStatic(RestClient.class)) {
                mockedRestClient.when(RestClient::create).thenReturn(restClient);
                when(restClient.post()).thenReturn(bodyUriSpec);
                when(bodyUriSpec.uri(configuration.getLoginEndpoint())).thenReturn(bodySpec);
                when(bodySpec.headers(any())).thenReturn(bodySpec);
                when(bodySpec.body(anyString())).thenReturn(bodySpec);
                when(bodySpec.retrieve()).thenReturn(responseSpec);
                when(responseSpec.body(String.class)).thenReturn("{\"token\":\"jwt-abc-123\"}");

                jwtAuth.authenticate(auth, result);

                verify(bodySpec).body(injectedBody);
            }
        }

        @Test
        void propagatesExceptionWhenEnvVariableIsMissing() throws IOException {
            File credFile = createCredentialsFile("{\"username\":\"${MISSING_VAR}\"}");
            when(fileSearcher.searchFileFromDataFolder("env-creds.json")).thenReturn(credFile);
            when(systemVariableService.inject(anyString()))
                    .thenThrow(new DefaultFrameworkException("Environment variable <MISSING_VAR> not found"));

            Auth auth = buildAuth("myApi", "env-creds.json");
            CommandResult result = new CommandResult();

            assertThrows(DefaultFrameworkException.class,
                    () -> jwtAuth.authenticate(auth, result));
        }
    }

    @Nested
    class LogResponseStatusError {

        @Test
        void handlesNotFoundStatusWithoutException() throws Exception {
            Method method = JwtAuth.class.getDeclaredMethod(
                    "logResponseStatusError", RestClientResponseException.class);
            method.setAccessible(true);

            HttpClientErrorException exception =
                    new HttpClientErrorException(HttpStatus.NOT_FOUND);
            assertDoesNotThrow(() -> method.invoke(jwtAuth, exception));
        }

        @Test
        void handlesBadGatewayStatusWithoutException() throws Exception {
            Method method = JwtAuth.class.getDeclaredMethod(
                    "logResponseStatusError", RestClientResponseException.class);
            method.setAccessible(true);

            HttpServerErrorException exception =
                    new HttpServerErrorException(HttpStatus.BAD_GATEWAY);
            assertDoesNotThrow(() -> method.invoke(jwtAuth, exception));
        }

        @Test
        void handlesOtherStatusWithoutException() throws Exception {
            Method method = JwtAuth.class.getDeclaredMethod(
                    "logResponseStatusError", RestClientResponseException.class);
            method.setAccessible(true);

            HttpClientErrorException exception =
                    new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
            assertDoesNotThrow(() -> method.invoke(jwtAuth, exception));
        }
    }

    @Nested
    class LogAuthInfo {

        @Test
        void logsAuthInfoWithoutException() throws IOException {
            File credFile = createCredentialsFile("{\"username\":\"admin\",\"password\":\"secret\"}");
            when(fileSearcher.searchFileFromDataFolder("creds.json")).thenReturn(credFile);


            Auth auth = buildAuth("myApi", "creds.json");
            CommandResult result = new CommandResult();

            RestClient restClient = mock(RestClient.class);
            RestClient.RequestBodyUriSpec bodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
            RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

            try (MockedStatic<RestClient> mockedRestClient = mockStatic(RestClient.class)) {
                mockedRestClient.when(RestClient::create).thenReturn(restClient);
                when(restClient.post()).thenReturn(bodyUriSpec);
                when(bodyUriSpec.uri(configuration.getLoginEndpoint())).thenReturn(bodySpec);
                when(bodySpec.headers(any())).thenReturn(bodySpec);
                when(bodySpec.body(anyString())).thenReturn(bodySpec);
                when(bodySpec.retrieve()).thenReturn(responseSpec);
                when(responseSpec.body(String.class)).thenReturn("{\"token\":\"t\"}");

                assertDoesNotThrow(() -> jwtAuth.authenticate(auth, result));
            }
        }
    }

    private File createCredentialsFile(final String content) throws IOException {
        Path file = tempDir.resolve("credentials.json");
        Files.writeString(file, content);
        return file.toFile();
    }

    private Auth buildAuth(final String alias, final String credentials) {
        Auth auth = new Auth();
        auth.setStrategyAlias(alias);
        auth.setCredentials(credentials);
        return auth;
    }

}

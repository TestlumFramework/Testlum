package com.knubisoft.testlum.testing.framework.auth;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.IntegrationsProvider;
import com.knubisoft.testlum.testing.model.global_config.Api;
import com.knubisoft.testlum.testing.model.scenario.Auth;
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
import org.springframework.web.client.RestClient;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthTest {

    @TempDir
    Path tempDir;

    private InterpreterDependencies dependencies;
    private IntegrationsProvider integrationsProvider;
    private FileSearcher fileSearcher;
    private JwtAuth jwtAuth;

    @BeforeEach
    void setUp() {
        dependencies = mock(InterpreterDependencies.class);
        ApplicationContext context = mock(ApplicationContext.class);
        integrationsProvider = mock(IntegrationsProvider.class);
        fileSearcher = mock(FileSearcher.class);

        when(dependencies.getContext()).thenReturn(context);
        when(dependencies.getEnvironment()).thenReturn("test");
        when(context.getBean(IntegrationsProvider.class)).thenReturn(integrationsProvider);
        when(context.getBean(FileSearcher.class)).thenReturn(fileSearcher);

        List<Api> apiList = List.of(buildApi("myApi", "http://localhost:8080", null));
        when(integrationsProvider.findListByEnv(Api.class, "test")).thenReturn(apiList);

        jwtAuth = new JwtAuth(dependencies);
    }

    @Nested
    class Authenticate {

        @Test
        void setsJwtAuthenticationTypeOnResult() throws IOException {
            File credFile = createCredentialsFile("{\"username\":\"admin\",\"password\":\"secret\"}");
            when(fileSearcher.searchFileFromDataFolder("creds.json")).thenReturn(credFile);

            Api api = buildApi("myApi", "http://localhost:8080", null);
            when(integrationsProvider.findApiForAlias(any(), eq("myApi"))).thenReturn(api);

            Auth auth = buildAuth("myApi", "creds.json", "/auth/login");
            CommandResult result = new CommandResult();

            RestClient restClient = mock(RestClient.class);
            RestClient.RequestBodyUriSpec bodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
            RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

            try (MockedStatic<RestClient> mockedRestClient = mockStatic(RestClient.class)) {
                mockedRestClient.when(RestClient::create).thenReturn(restClient);
                when(restClient.post()).thenReturn(bodyUriSpec);
                when(bodyUriSpec.uri(anyString())).thenReturn(bodySpec);
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

            Api api = buildApi("myApi", "http://localhost:8080", null);
            when(integrationsProvider.findApiForAlias(any(), eq("myApi"))).thenReturn(api);

            Auth auth = buildAuth("myApi", "creds.json", "/auth/login");
            CommandResult result = new CommandResult();

            RestClient restClient = mock(RestClient.class);
            RestClient.RequestBodyUriSpec bodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
            RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

            try (MockedStatic<RestClient> mockedRestClient = mockStatic(RestClient.class)) {
                mockedRestClient.when(RestClient::create).thenReturn(restClient);
                when(restClient.post()).thenReturn(bodyUriSpec);
                when(bodyUriSpec.uri(anyString())).thenReturn(bodySpec);
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
        void usesCustomTokenNameFromApiConfig() throws IOException {
            File credFile = createCredentialsFile("{\"username\":\"admin\",\"password\":\"secret\"}");
            when(fileSearcher.searchFileFromDataFolder("creds.json")).thenReturn(credFile);

            Api api = buildApi("myApi", "http://localhost:8080", "access_token");
            when(integrationsProvider.findApiForAlias(any(), eq("myApi"))).thenReturn(api);

            Auth auth = buildAuth("myApi", "creds.json", "/auth/login");
            CommandResult result = new CommandResult();

            RestClient restClient = mock(RestClient.class);
            RestClient.RequestBodyUriSpec bodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
            RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

            try (MockedStatic<RestClient> mockedRestClient = mockStatic(RestClient.class)) {
                mockedRestClient.when(RestClient::create).thenReturn(restClient);
                when(restClient.post()).thenReturn(bodyUriSpec);
                when(bodyUriSpec.uri(anyString())).thenReturn(bodySpec);
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
        void setsEmptyTokenWhenResponseIsBlank() throws IOException {
            File credFile = createCredentialsFile("{\"username\":\"admin\",\"password\":\"secret\"}");
            when(fileSearcher.searchFileFromDataFolder("creds.json")).thenReturn(credFile);

            Api api = buildApi("myApi", "http://localhost:8080", null);
            when(integrationsProvider.findApiForAlias(any(), eq("myApi"))).thenReturn(api);

            Auth auth = buildAuth("myApi", "creds.json", "/auth/login");
            CommandResult result = new CommandResult();

            RestClient restClient = mock(RestClient.class);
            RestClient.RequestBodyUriSpec bodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
            RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

            try (MockedStatic<RestClient> mockedRestClient = mockStatic(RestClient.class)) {
                mockedRestClient.when(RestClient::create).thenReturn(restClient);
                when(restClient.post()).thenReturn(bodyUriSpec);
                when(bodyUriSpec.uri(anyString())).thenReturn(bodySpec);
                when(bodySpec.headers(any())).thenReturn(bodySpec);
                when(bodySpec.body(anyString())).thenReturn(bodySpec);
                when(bodySpec.retrieve()).thenReturn(responseSpec);
                when(responseSpec.body(String.class)).thenReturn("");

                jwtAuth.authenticate(auth, result);
            }

            ArgumentCaptor<InterpreterDependencies.Authorization> captor =
                    ArgumentCaptor.forClass(InterpreterDependencies.Authorization.class);
            verify(dependencies).setAuthorization(captor.capture());

            String headerValue = captor.getValue().getHeaders()
                    .get(AuthorizationConstant.HEADER_AUTHORIZATION);
            assertEquals("Bearer ", headerValue);
        }

        @Test
        void setsEmptyTokenWhenResponseIsNull() throws IOException {
            File credFile = createCredentialsFile("{\"username\":\"admin\",\"password\":\"secret\"}");
            when(fileSearcher.searchFileFromDataFolder("creds.json")).thenReturn(credFile);

            Api api = buildApi("myApi", "http://localhost:8080", null);
            when(integrationsProvider.findApiForAlias(any(), eq("myApi"))).thenReturn(api);

            Auth auth = buildAuth("myApi", "creds.json", "/auth/login");
            CommandResult result = new CommandResult();

            RestClient restClient = mock(RestClient.class);
            RestClient.RequestBodyUriSpec bodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
            RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

            try (MockedStatic<RestClient> mockedRestClient = mockStatic(RestClient.class)) {
                mockedRestClient.when(RestClient::create).thenReturn(restClient);
                when(restClient.post()).thenReturn(bodyUriSpec);
                when(bodyUriSpec.uri(anyString())).thenReturn(bodySpec);
                when(bodySpec.headers(any())).thenReturn(bodySpec);
                when(bodySpec.body(anyString())).thenReturn(bodySpec);
                when(bodySpec.retrieve()).thenReturn(responseSpec);
                when(responseSpec.body(String.class)).thenReturn(null);

                jwtAuth.authenticate(auth, result);
            }

            ArgumentCaptor<InterpreterDependencies.Authorization> captor =
                    ArgumentCaptor.forClass(InterpreterDependencies.Authorization.class);
            verify(dependencies).setAuthorization(captor.capture());

            String headerValue = captor.getValue().getHeaders()
                    .get(AuthorizationConstant.HEADER_AUTHORIZATION);
            assertEquals("Bearer ", headerValue);
        }
    }

    @Nested
    class DoRequestErrorHandling {

        @Test
        void returnsEmptyStringOnHttpClientErrorNotFound() throws IOException {
            File credFile = createCredentialsFile("{\"username\":\"admin\",\"password\":\"secret\"}");
            when(fileSearcher.searchFileFromDataFolder("creds.json")).thenReturn(credFile);

            Api api = buildApi("myApi", "http://localhost:8080", null);
            when(integrationsProvider.findApiForAlias(any(), eq("myApi"))).thenReturn(api);

            Auth auth = buildAuth("myApi", "creds.json", "/auth/login");
            CommandResult result = new CommandResult();

            RestClient restClient = mock(RestClient.class);
            RestClient.RequestBodyUriSpec bodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);

            try (MockedStatic<RestClient> mockedRestClient = mockStatic(RestClient.class)) {
                mockedRestClient.when(RestClient::create).thenReturn(restClient);
                when(restClient.post()).thenReturn(bodyUriSpec);
                when(bodyUriSpec.uri(anyString())).thenReturn(bodySpec);
                when(bodySpec.headers(any())).thenReturn(bodySpec);
                when(bodySpec.body(anyString())).thenReturn(bodySpec);
                when(bodySpec.retrieve()).thenThrow(
                        new HttpClientErrorException(HttpStatus.NOT_FOUND));

                jwtAuth.authenticate(auth, result);
            }

            ArgumentCaptor<InterpreterDependencies.Authorization> captor =
                    ArgumentCaptor.forClass(InterpreterDependencies.Authorization.class);
            verify(dependencies).setAuthorization(captor.capture());

            String headerValue = captor.getValue().getHeaders()
                    .get(AuthorizationConstant.HEADER_AUTHORIZATION);
            assertEquals("Bearer ", headerValue);
        }

        @Test
        void returnsEmptyStringOnHttpClientErrorBadGateway() throws IOException {
            File credFile = createCredentialsFile("{\"username\":\"admin\",\"password\":\"secret\"}");
            when(fileSearcher.searchFileFromDataFolder("creds.json")).thenReturn(credFile);

            Api api = buildApi("myApi", "http://localhost:8080", null);
            when(integrationsProvider.findApiForAlias(any(), eq("myApi"))).thenReturn(api);

            Auth auth = buildAuth("myApi", "creds.json", "/auth/login");
            CommandResult result = new CommandResult();

            RestClient restClient = mock(RestClient.class);
            RestClient.RequestBodyUriSpec bodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);

            try (MockedStatic<RestClient> mockedRestClient = mockStatic(RestClient.class)) {
                mockedRestClient.when(RestClient::create).thenReturn(restClient);
                when(restClient.post()).thenReturn(bodyUriSpec);
                when(bodyUriSpec.uri(anyString())).thenReturn(bodySpec);
                when(bodySpec.headers(any())).thenReturn(bodySpec);
                when(bodySpec.body(anyString())).thenReturn(bodySpec);
                when(bodySpec.retrieve()).thenThrow(
                        new HttpClientErrorException(HttpStatus.BAD_GATEWAY));

                jwtAuth.authenticate(auth, result);
            }

            ArgumentCaptor<InterpreterDependencies.Authorization> captor =
                    ArgumentCaptor.forClass(InterpreterDependencies.Authorization.class);
            verify(dependencies).setAuthorization(captor.capture());

            String headerValue = captor.getValue().getHeaders()
                    .get(AuthorizationConstant.HEADER_AUTHORIZATION);
            assertEquals("Bearer ", headerValue);
        }

        @Test
        void returnsEmptyStringOnHttpClientErrorOtherStatus() throws IOException {
            File credFile = createCredentialsFile("{\"username\":\"admin\",\"password\":\"secret\"}");
            when(fileSearcher.searchFileFromDataFolder("creds.json")).thenReturn(credFile);

            Api api = buildApi("myApi", "http://localhost:8080", null);
            when(integrationsProvider.findApiForAlias(any(), eq("myApi"))).thenReturn(api);

            Auth auth = buildAuth("myApi", "creds.json", "/auth/login");
            CommandResult result = new CommandResult();

            RestClient restClient = mock(RestClient.class);
            RestClient.RequestBodyUriSpec bodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);

            try (MockedStatic<RestClient> mockedRestClient = mockStatic(RestClient.class)) {
                mockedRestClient.when(RestClient::create).thenReturn(restClient);
                when(restClient.post()).thenReturn(bodyUriSpec);
                when(bodyUriSpec.uri(anyString())).thenReturn(bodySpec);
                when(bodySpec.headers(any())).thenReturn(bodySpec);
                when(bodySpec.body(anyString())).thenReturn(bodySpec);
                when(bodySpec.retrieve()).thenThrow(
                        new HttpClientErrorException(HttpStatus.FORBIDDEN));

                jwtAuth.authenticate(auth, result);
            }

            ArgumentCaptor<InterpreterDependencies.Authorization> captor =
                    ArgumentCaptor.forClass(InterpreterDependencies.Authorization.class);
            verify(dependencies).setAuthorization(captor.capture());

            String headerValue = captor.getValue().getHeaders()
                    .get(AuthorizationConstant.HEADER_AUTHORIZATION);
            assertEquals("Bearer ", headerValue);
        }
    }

    @Nested
    class GetCredentialsFromFile {

        @Test
        void throwsDefaultFrameworkExceptionOnIOException() {
            when(fileSearcher.searchFileFromDataFolder("missing.json"))
                    .thenReturn(new File("/nonexistent/path/missing.json"));

            Auth auth = buildAuth("myApi", "missing.json", "/auth/login");
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
                    "logResponseStatusError", HttpClientErrorException.class);
            method.setAccessible(true);

            HttpClientErrorException exception =
                    new HttpClientErrorException(HttpStatus.NOT_FOUND);
            assertDoesNotThrow(() -> method.invoke(jwtAuth, exception));
        }

        @Test
        void handlesBadGatewayStatusWithoutException() throws Exception {
            Method method = JwtAuth.class.getDeclaredMethod(
                    "logResponseStatusError", HttpClientErrorException.class);
            method.setAccessible(true);

            HttpClientErrorException exception =
                    new HttpClientErrorException(HttpStatus.BAD_GATEWAY);
            assertDoesNotThrow(() -> method.invoke(jwtAuth, exception));
        }

        @Test
        void handlesOtherStatusWithoutException() throws Exception {
            Method method = JwtAuth.class.getDeclaredMethod(
                    "logResponseStatusError", HttpClientErrorException.class);
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

            Api api = buildApi("myApi", "http://localhost:8080", null);
            when(integrationsProvider.findApiForAlias(any(), eq("myApi"))).thenReturn(api);

            Auth auth = buildAuth("myApi", "creds.json", "/api/login");
            CommandResult result = new CommandResult();

            RestClient restClient = mock(RestClient.class);
            RestClient.RequestBodyUriSpec bodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
            RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

            try (MockedStatic<RestClient> mockedRestClient = mockStatic(RestClient.class)) {
                mockedRestClient.when(RestClient::create).thenReturn(restClient);
                when(restClient.post()).thenReturn(bodyUriSpec);
                when(bodyUriSpec.uri(anyString())).thenReturn(bodySpec);
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

    private Auth buildAuth(final String alias, final String credentials, final String endpoint) {
        Auth auth = new Auth();
        auth.setApiAlias(alias);
        auth.setCredentials(credentials);
        auth.setLoginEndpoint(endpoint);
        return auth;
    }

    private Api buildApi(final String alias, final String url, final String tokenName) {
        Api api = new Api();
        api.setAlias(alias);
        api.setUrl(url);
        com.knubisoft.testlum.testing.model.global_config.Auth authConfig =
                new com.knubisoft.testlum.testing.model.global_config.Auth();
        if (tokenName != null) {
            authConfig.setTokenName(tokenName);
        }
        api.setAuth(authConfig);
        return api;
    }
}

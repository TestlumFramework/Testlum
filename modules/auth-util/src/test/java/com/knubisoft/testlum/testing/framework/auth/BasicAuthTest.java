package com.knubisoft.testlum.testing.framework.auth;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.Auth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BasicAuthTest {

    @TempDir
    Path tempDir;

    private InterpreterDependencies dependencies;
    private FileSearcher fileSearcher;
    private BasicAuth basicAuth;

    @BeforeEach
    void setUp() {
        dependencies = mock(InterpreterDependencies.class);
        ApplicationContext context = mock(ApplicationContext.class);
        fileSearcher = mock(FileSearcher.class);

        when(dependencies.getContext()).thenReturn(context);
        when(context.getBean(FileSearcher.class)).thenReturn(fileSearcher);

        basicAuth = new BasicAuth(dependencies);
    }

    @Nested
    class Authenticate {

        @Test
        void setsBasicAuthHeaderWithBase64EncodedCredentials() throws IOException {
            File credFile = createCredentialsFile("admin", "secret");
            when(fileSearcher.searchFileFromDataFolder("creds.json")).thenReturn(credFile);

            Auth auth = buildAuth("myApi", "creds.json", "/login");
            CommandResult result = new CommandResult();

            basicAuth.authenticate(auth, result);

            ArgumentCaptor<InterpreterDependencies.Authorization> captor =
                    ArgumentCaptor.forClass(InterpreterDependencies.Authorization.class);
            verify(dependencies).setAuthorization(captor.capture());

            String expectedEncoded = Base64.getEncoder()
                    .encodeToString("admin:secret".getBytes(StandardCharsets.UTF_8));
            String headerValue = captor.getValue().getHeaders()
                    .get(AuthorizationConstant.HEADER_AUTHORIZATION);
            assertEquals("Basic " + expectedEncoded, headerValue);
        }

        @Test
        void putsAuthenticationTypeMetadataOnResult() throws IOException {
            File credFile = createCredentialsFile("user", "pass");
            when(fileSearcher.searchFileFromDataFolder("creds.json")).thenReturn(credFile);

            Auth auth = buildAuth("myApi", "creds.json", "/login");
            CommandResult result = new CommandResult();

            basicAuth.authenticate(auth, result);

            assertEquals(AuthorizationConstant.HEADER_BASIC,
                    result.getMetadata().get("Authentication type"));
        }

        @Test
        void handlesSpecialCharactersInCredentials() throws IOException {
            File credFile = createCredentialsFile("user@domain.com", "p@ss:w0rd!");
            when(fileSearcher.searchFileFromDataFolder("special.json")).thenReturn(credFile);

            Auth auth = buildAuth("myApi", "special.json", "/login");
            CommandResult result = new CommandResult();

            basicAuth.authenticate(auth, result);

            ArgumentCaptor<InterpreterDependencies.Authorization> captor =
                    ArgumentCaptor.forClass(InterpreterDependencies.Authorization.class);
            verify(dependencies).setAuthorization(captor.capture());

            String expectedEncoded = Base64.getEncoder()
                    .encodeToString("user@domain.com:p@ss:w0rd!".getBytes(StandardCharsets.UTF_8));
            String headerValue = captor.getValue().getHeaders()
                    .get(AuthorizationConstant.HEADER_AUTHORIZATION);
            assertEquals("Basic " + expectedEncoded, headerValue);
        }
    }

    @Nested
    class GetCredentialsFromFile {

        @Test
        void throwsDefaultFrameworkExceptionOnIOException() {
            when(fileSearcher.searchFileFromDataFolder("missing.json"))
                    .thenReturn(new File("/nonexistent/path/missing.json"));

            Auth auth = buildAuth("myApi", "missing.json", "/login");
            CommandResult result = new CommandResult();

            assertThrows(DefaultFrameworkException.class,
                    () -> basicAuth.authenticate(auth, result));
        }
    }

    @Nested
    class LogAuthInfo {

        @Test
        void logsWithoutException() throws IOException {
            File credFile = createCredentialsFile("admin", "secret");
            when(fileSearcher.searchFileFromDataFolder("creds.json")).thenReturn(credFile);

            Auth auth = buildAuth("testAlias", "creds.json", "/api/login");
            CommandResult result = new CommandResult();

            assertDoesNotThrow(() -> basicAuth.authenticate(auth, result));
        }
    }

    private File createCredentialsFile(final String username, final String password) throws IOException {
        Path file = tempDir.resolve("credentials.json");
        String json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
        Files.writeString(file, json);
        return file.toFile();
    }

    private Auth buildAuth(final String alias, final String credentials, final String endpoint) {
        Auth auth = new Auth();
        auth.setApiAlias(alias);
        auth.setCredentials(credentials);
        auth.setLoginEndpoint(endpoint);
        return auth;
    }
}

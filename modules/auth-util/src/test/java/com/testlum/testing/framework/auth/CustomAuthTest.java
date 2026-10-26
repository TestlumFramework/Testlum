package com.testlum.testing.framework.auth;

import com.jayway.jsonpath.PathNotFoundException;
import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.util.SystemVariableService;
import com.testlum.testing.model.global_config.CustomAuthStrategy;
import com.testlum.testing.model.scenario.Auth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CustomAuthTest {

    @TempDir
    private Path directory;
    private InterpreterDependencies dependencies;
    private RestTemplate restTemplate;
    private CustomAuthStrategy configuration;
    private CustomAuth strategy;
    private Auth command;

    @BeforeEach
    void setUp() throws Exception {
        dependencies = mock(InterpreterDependencies.class);
        ApplicationContext context = mock(ApplicationContext.class);
        when(dependencies.getContext()).thenReturn(context);
        FileSearcher searcher = mock(FileSearcher.class);
        SystemVariableService variables = mock(SystemVariableService.class);
        restTemplate = mock(RestTemplate.class);
        when(context.getBean(FileSearcher.class)).thenReturn(searcher);
        when(context.getBean(SystemVariableService.class)).thenReturn(variables);
        when(context.getBean(RestTemplate.class)).thenReturn(restTemplate);
        Path file = Files.writeString(directory.resolve("creds.json"), "{\"password\":\"${PASSWORD}\"}");
        when(searcher.searchFileFromDataFolder("creds.json")).thenReturn(file.toFile());
        when(variables.inject(anyString())).thenReturn("{\"password\":\"secret\"}");
        configuration = new CustomAuthStrategy();
        configuration.setAlias("CUSTOM");
        configuration.setLoginEndpoint("http://localhost/custom/login");
        strategy = new CustomAuth(dependencies, configuration);
        command = new Auth();
        command.setCredentials("creds.json");
    }

    @Test
    void authenticatesUsingStrategyEndpointAndResolvedCredentials() {
        response("{\"token\":\"custom-value\"}");
        CommandResult result = new CommandResult();
        strategy.authenticate(command, result);
        var authorization = ArgumentCaptor.forClass(InterpreterDependencies.Authorization.class);
        verify(dependencies).setAuthorization(authorization.capture());
        assertEquals("Custom custom-value", authorization.getValue().getHeaders().get("Authorization"));
        assertEquals("CUSTOM", result.getMetadata().get("Authentication type"));
        verify(restTemplate).postForObject(eq(configuration.getLoginEndpoint()),
                argThat((HttpEntity<?> request) -> "{\"password\":\"secret\"}".equals(request.getBody())),
                eq(String.class));
    }

    @Test
    void rejectsEmptyResponse() {
        response("");
        assertThrows(DefaultFrameworkException.class, () -> strategy.authenticate(command, new CommandResult()));
        verify(dependencies, never()).setAuthorization(any());
    }

    @Test
    void rejectsEmptyToken() {
        response("{\"token\":\"\"}");
        assertThrows(DefaultFrameworkException.class, () -> strategy.authenticate(command, new CommandResult()));
        verify(dependencies, never()).setAuthorization(any());
    }

    @Test
    void rejectsMissingToken() {
        response("{}");
        assertThrows(PathNotFoundException.class, () -> strategy.authenticate(command, new CommandResult()));
        verify(dependencies, never()).setAuthorization(any());
    }

    @Test
    void rejectsNonStringToken() {
        response("{\"token\":123}");
        assertThrows(DefaultFrameworkException.class, () -> strategy.authenticate(command, new CommandResult()));
        verify(dependencies, never()).setAuthorization(any());
    }

    @Test
    void requiresEndpointForThisCustomImplementation() {
        configuration.setLoginEndpoint(null);
        assertThrows(DefaultFrameworkException.class, () -> strategy.authenticate(command, new CommandResult()));
        verifyNoInteractions(restTemplate);
    }

    @Test
    void propagatesLoginFailureWithoutInstallingAuthorization() {
        HttpClientErrorException failure = new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class))).thenThrow(failure);
        assertSame(failure, assertThrows(HttpClientErrorException.class,
                () -> strategy.authenticate(command, new CommandResult())));
        verify(dependencies, never()).setAuthorization(any());
    }

    private void response(final String body) {
        when(restTemplate.postForObject(eq(configuration.getLoginEndpoint()), any(HttpEntity.class), eq(String.class)))
                .thenReturn(body);
    }
}

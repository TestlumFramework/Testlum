package com.knubisoft.e2e.testing.framework.interpreter.lib.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.knubisoft.e2e.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.model.scenario.Auth;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.knubisoft.e2e.testing.framework.constant.AuthorizationConstant.HEADER_AUTHORIZATION;
import static com.knubisoft.e2e.testing.framework.constant.AuthorizationConstant.HEADER_BASIC;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.CREDENTIALS_LOG;

@Slf4j
public class BasicAuth implements AuthStrategy {

    @Override
    public void login(final InterpreterDependencies dependencies, final Auth auth, final CommandResult result) {
        String credentials = encodedCredentials(auth, dependencies);
        setAuthHeaders(dependencies, credentials);
    }

    @Override
    public void logout(final InterpreterDependencies dependencies) {
        InterpreterDependencies.Authorization authorization = new InterpreterDependencies.Authorization();
        authorization.setHeaders(Collections.emptyMap());
        dependencies.setAuthorization(authorization);
    }

    @SneakyThrows
    private String encodedCredentials(final Auth auth, final InterpreterDependencies dependencies) {
        String credentials = getCredentialsFromFile(auth, dependencies);
        final ObjectNode node = new ObjectMapper().readValue(credentials, ObjectNode.class);
        credentials = (node.get("username") + ":" + node.get("password")).replaceAll("\"", "");
        log.info(CREDENTIALS_LOG, credentials);
        return Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private String getCredentialsFromFile(final Auth auth, final InterpreterDependencies dependencies) {
        File credentialsFolder = TestResourceSettings.getInstance().getCredentialsFolder();
        return dependencies.getFileSearcher()
                .searchFileAndReadToString(credentialsFolder, auth.getCredentials());
    }

    private void setAuthHeaders(final InterpreterDependencies dependencies, final String credentials) {
        final InterpreterDependencies.Authorization authorization = new InterpreterDependencies.Authorization();
        final Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_AUTHORIZATION, HEADER_BASIC + credentials);
        authorization.setHeaders(headers);
        dependencies.setAuthorization(authorization);
    }


}

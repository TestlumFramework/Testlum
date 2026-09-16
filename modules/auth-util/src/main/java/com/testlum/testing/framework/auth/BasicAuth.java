package com.testlum.testing.framework.auth;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.constant.DelimiterConstant;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.util.SystemVariableService;
import com.testlum.testing.model.scenario.Auth;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
public class BasicAuth extends AbstractAuthStrategy {

    private final FileSearcher fileSearcher;
    private final SystemVariableService systemVariableService;

    public BasicAuth(final InterpreterDependencies dependencies) {
        super(dependencies);
        this.fileSearcher = dependencies.getContext().getBean(FileSearcher.class);
        this.systemVariableService = dependencies.getContext().getBean(SystemVariableService.class);
    }

    @Override
    public void authenticate(final Auth auth, final CommandResult result) {
        result.put(AUTHENTICATION_TYPE, AuthorizationConstant.HEADER_BASIC);
        String credentials = encodedCredentials(auth);
        login(credentials, AuthorizationConstant.HEADER_BASIC);
    }

    private String encodedCredentials(final Auth auth) {
        String credentials = getCredentialsFromFile(auth.getCredentials());
        DocumentContext context = JsonPath.parse(credentials);
        credentials = context.read(AuthorizationConstant.USERNAME_JPATH)
                + DelimiterConstant.COLON + context.read(AuthorizationConstant.PASSWORD_JPATH);
        logAuthInfo(auth);
        return Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private String getCredentialsFromFile(final String fileName) {
        try {
            String content = FileUtils.readFileToString(fileSearcher.searchFileFromDataFolder(fileName),
                    StandardCharsets.UTF_8);
            return systemVariableService.inject(content);
        } catch (IOException e) {
            throw new DefaultFrameworkException(e);
        }
    }

    private void logAuthInfo(final Auth auth) {
        log.info(ALIAS_LOG, auth.getApiAlias());
        log.info(ENDPOINT_LOG, auth.getLoginEndpoint());
        log.info(CREDENTIALS_LOG, auth.getCredentials());
    }
}

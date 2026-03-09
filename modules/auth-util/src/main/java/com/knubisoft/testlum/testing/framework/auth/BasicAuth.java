package com.knubisoft.testlum.testing.framework.auth;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.knubisoft.testlum.log.LogFormat;
import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.Auth;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static com.knubisoft.testlum.testing.framework.auth.AuthorizationConstant.*;

@Slf4j
public class BasicAuth extends AbstractAuthStrategy {

    private static final String ALIAS_LOG = LogFormat.table("Alias");
    private static final String ENDPOINT_LOG = LogFormat.table("Endpoint");
    private static final String CREDENTIALS_LOG = LogFormat.table("Credentials");

    private static final String AUTHENTICATION_TYPE = "Authentication type";
    private final FileSearcher fileSearcher;

    public BasicAuth(final InterpreterDependencies dependencies) {
        super(dependencies);
        this.fileSearcher = dependencies.getContext().getBean(FileSearcher.class);
    }

    @Override
    public void authenticate(final Auth auth, final CommandResult result) {
        result.put(AUTHENTICATION_TYPE, HEADER_BASIC);
        String credentials = encodedCredentials(auth);
        login(credentials, HEADER_BASIC);
    }

    private String encodedCredentials(final Auth auth) {
        String credentials = getCredentialsFromFile(auth.getCredentials());
        DocumentContext context = JsonPath.parse(credentials);
        credentials = context.read(USERNAME_JPATH) + DelimiterConstant.COLON + context.read(PASSWORD_JPATH);
        logAuthInfo(auth);
        return Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    @SneakyThrows
    private String getCredentialsFromFile(final String fileName) {
        return FileUtils.readFileToString(fileSearcher.searchFileFromDataFolder(fileName), StandardCharsets.UTF_8);
    }

    private void logAuthInfo(final Auth auth) {
        log.info(ALIAS_LOG, auth.getApiAlias());
        log.info(ENDPOINT_LOG, auth.getLoginEndpoint());
        log.info(CREDENTIALS_LOG, auth.getCredentials());
    }
}

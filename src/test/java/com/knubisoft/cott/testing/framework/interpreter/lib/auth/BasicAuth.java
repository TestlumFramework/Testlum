package com.knubisoft.cott.testing.framework.interpreter.lib.auth;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.AuthUtil;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.model.scenario.Auth;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static com.knubisoft.cott.testing.framework.constant.AuthorizationConstant.HEADER_BASIC;
import static com.knubisoft.cott.testing.framework.constant.AuthorizationConstant.PASSWORD_JPATH;
import static com.knubisoft.cott.testing.framework.constant.AuthorizationConstant.USERNAME_JPATH;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.AUTHENTICATION_TYPE;

@Slf4j
public class BasicAuth extends AbstractAuthStrategy {

    public BasicAuth(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void authenticate(final Auth auth, final CommandResult result) {
        result.put(AUTHENTICATION_TYPE, HEADER_BASIC);
        String credentials = encodedCredentials(auth);
        login(credentials, HEADER_BASIC);
    }

    private String encodedCredentials(final Auth auth) {
        String credentials = AuthUtil.getCredentialsFromFile(auth.getCredentials());
        DocumentContext context = JsonPath.parse(credentials);
        credentials = context.read(USERNAME_JPATH) + DelimiterConstant.COLON + context.read(PASSWORD_JPATH);
        LogUtil.logAuthInfo(auth);
        return Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }
}

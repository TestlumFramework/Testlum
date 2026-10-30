package com.testlum.testing.framework.auth;

import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.model.global_config.GoogleAuthStrategy;
import com.testlum.testing.model.scenario.Auth;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GoogleAuth extends AbstractAuthStrategy {

    private final GoogleAuthStrategy configuration;
    private final GoogleCredentialsReader credentialsReader;
    private final GoogleTokenClient tokenClient;

    public GoogleAuth(final InterpreterDependencies dependencies, final GoogleAuthStrategy configuration) {
        super(dependencies);
        this.configuration = configuration;
        this.credentialsReader = dependencies.getContext().getBean(GoogleCredentialsReader.class);
        this.tokenClient = dependencies.getContext().getBean(GoogleTokenClient.class);
    }

    @Override
    public void authenticate(final Auth auth, final CommandResult result) {
        log.info(ALIAS_LOG, configuration.getAlias());
        log.info(CREDENTIALS_LOG, auth.getCredentials());
        String refreshToken = credentialsReader.readRefreshToken(auth.getCredentials());
        GoogleTokenResponse response = tokenClient.refreshToken(configuration, refreshToken);
        login(response.getAccessToken(), AuthorizationConstant.HEADER_BEARER);
        result.put(AUTHENTICATION_TYPE, "GOOGLE");
    }
}

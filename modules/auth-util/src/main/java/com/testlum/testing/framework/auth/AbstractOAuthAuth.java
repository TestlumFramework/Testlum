package com.testlum.testing.framework.auth;

import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.model.global_config.AuthStrategy;
import com.testlum.testing.model.scenario.Auth;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractOAuthAuth<C extends AuthStrategy> extends AbstractAuthStrategy {

    private final C configuration;
    private final OAuthCredentialsReader credentialsReader;
    private final AbstractOAuthTokenClient<C> tokenClient;

    protected AbstractOAuthAuth(final InterpreterDependencies dependencies, final C configuration,
                                final AbstractOAuthTokenClient<C> tokenClient) {
        super(dependencies);
        this.configuration = configuration;
        this.credentialsReader = dependencies.getContext().getBean(OAuthCredentialsReader.class);
        this.tokenClient = tokenClient;
    }

    protected abstract String authenticationType();

    @Override
    public final void authenticate(final Auth auth, final CommandResult result) {
        log.info(ALIAS_LOG, configuration.getAlias());
        log.info(CREDENTIALS_LOG, auth.getCredentials());
        String refreshToken = credentialsReader.readRefreshToken(auth.getCredentials());
        OAuthTokenResponse response = tokenClient.refreshToken(configuration, refreshToken);
        login(response.getAccessToken(), AuthorizationConstant.HEADER_BEARER);
        result.put(AUTHENTICATION_TYPE, authenticationType());
    }
}

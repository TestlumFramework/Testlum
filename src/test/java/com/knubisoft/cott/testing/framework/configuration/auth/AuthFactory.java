package com.knubisoft.cott.testing.framework.configuration.auth;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.auth.AuthStrategy;
import com.knubisoft.cott.testing.framework.interpreter.lib.auth.BasicAuth;
import com.knubisoft.cott.testing.framework.interpreter.lib.auth.DefaultStrategy;
import com.knubisoft.cott.testing.framework.interpreter.lib.auth.JwtAuth;
import com.knubisoft.cott.testing.framework.interpreter.lib.auth.OAuth2Auth;
import com.knubisoft.cott.testing.model.global_config.Auth;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.NotImplementedException;

import static java.util.Objects.nonNull;

@UtilityClass
public class AuthFactory {
    public AuthStrategy create(final InterpreterDependencies dependencies) {
        final Auth auth = GlobalTestConfigurationProvider.provide().getAuth();

        switch (auth.getAuthStrategy()) {
            case BASIC:
                return new BasicAuth(dependencies);
            case OAUTH_2:
                if (nonNull(GlobalTestConfigurationProvider.provide().getAuth().getOauth2())) {
                    return new OAuth2Auth(dependencies);
                }
                throw new RuntimeException("You have to set a mandatory tag " + System.lineSeparator()
                        + "<auth>" + System.lineSeparator()
                        + "<oauth2 clientId='' clientSecret='' tokenAccessUri=''/>" + System.lineSeparator()
                        + "</auth>");
            case JWT:
                return new JwtAuth(dependencies);
            case CUSTOM:
                return createCustomStrategy(auth.getAuthCustomClassName());
            default:
                return new DefaultStrategy(dependencies);
        }
    }

    private AuthStrategy createCustomStrategy(final String className) {
        try {
            return (AuthStrategy) Class.forName(className).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
           throw new NotImplementedException();
        }
    }
}

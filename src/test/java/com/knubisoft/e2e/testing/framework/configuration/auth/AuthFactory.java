package com.knubisoft.e2e.testing.framework.configuration.auth;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.auth.AuthStrategy;
import com.knubisoft.e2e.testing.framework.interpreter.lib.auth.BasicAuth;
import com.knubisoft.e2e.testing.framework.interpreter.lib.auth.DefaultStrategy;
import com.knubisoft.e2e.testing.framework.interpreter.lib.auth.JwtAuth;
import com.knubisoft.e2e.testing.model.global_config.Auth;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.NotImplementedException;

@UtilityClass
public class AuthFactory {
    public AuthStrategy create(final InterpreterDependencies dependencies) {
        final Auth auth = GlobalTestConfigurationProvider.provide().getAuth();
        // TODO i.doroshenko add OAUTH_2 to switch-case
        switch (auth.getAuthStrategy()) {
            case BASIC:
                return new BasicAuth(dependencies);
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

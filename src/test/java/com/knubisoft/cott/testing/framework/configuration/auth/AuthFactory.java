package com.knubisoft.cott.testing.framework.configuration.auth;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.auth.AuthStrategy;
import com.knubisoft.cott.testing.framework.interpreter.lib.auth.BasicAuth;
import com.knubisoft.cott.testing.framework.interpreter.lib.auth.DefaultStrategy;
import com.knubisoft.cott.testing.framework.interpreter.lib.auth.JwtAuth;
import com.knubisoft.cott.testing.framework.util.ConfigUtil;
import com.knubisoft.cott.testing.model.global_config.Api;
import com.knubisoft.cott.testing.model.global_config.Auth;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.NotImplementedException;

import java.util.List;

@UtilityClass
public class AuthFactory {
    public AuthStrategy create(final InterpreterDependencies dependencies, final String alias) {
        final Auth auth = getAuthConfig(alias);
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

    private static Auth getAuthConfig(final String alias) {
        List<Api> apiList = GlobalTestConfigurationProvider.getIntegrations().getApis().getApi();
        Api apiIntegration = (Api) ConfigUtil.findApiForAlias(apiList, alias);
        return apiIntegration.getAuth();
    }

    private AuthStrategy createCustomStrategy(final String className) {
        try {
            return (AuthStrategy) Class.forName(className).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new NotImplementedException();
        }
    }
}

package com.knubisoft.cott.testing.framework.configuration.auth;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.auth.AuthStrategy;
import com.knubisoft.cott.testing.framework.interpreter.lib.auth.BasicAuth;
import com.knubisoft.cott.testing.framework.interpreter.lib.auth.DefaultStrategy;
import com.knubisoft.cott.testing.framework.interpreter.lib.auth.JwtAuth;
import com.knubisoft.cott.testing.framework.util.IntegrationsUtil;
import com.knubisoft.cott.testing.model.global_config.Api;
import com.knubisoft.cott.testing.model.global_config.Auth;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.NotImplementedException;

import java.util.List;
import java.util.Objects;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.AUTH_NOT_FOUND;

@UtilityClass
public class AuthFactory {

    public AuthStrategy create(final InterpreterDependencies dependencies, final String alias) {
        final Auth auth = getAuthConfig(dependencies.getEnvironment(), alias);

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

    private Auth getAuthConfig(final String env, final String alias) {
        List<Api> apiList = GlobalTestConfigurationProvider.getIntegrations().get(env).getApis().getApi();
        Api apiIntegration = IntegrationsUtil.findApiForAlias(apiList, alias);
        if (Objects.nonNull(apiIntegration.getAuth())) {
            return apiIntegration.getAuth();
        }
        throw new DefaultFrameworkException(AUTH_NOT_FOUND, apiIntegration.getAlias());
    }

    private AuthStrategy createCustomStrategy(final String className) {
        try {
            return (AuthStrategy) Class.forName(className).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new NotImplementedException();
        }
    }
}

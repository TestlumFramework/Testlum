package com.knubisoft.testlum.testing.framework.configuration.auth;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.auth.AuthStrategy;
import com.knubisoft.testlum.testing.framework.interpreter.lib.auth.BasicAuth;
import com.knubisoft.testlum.testing.framework.interpreter.lib.auth.DefaultStrategy;
import com.knubisoft.testlum.testing.framework.interpreter.lib.auth.JwtAuth;
import com.knubisoft.testlum.testing.framework.util.IntegrationsUtil;
import com.knubisoft.testlum.testing.model.global_config.Api;
import com.knubisoft.testlum.testing.model.global_config.Auth;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.NotImplementedException;

import java.util.List;
import java.util.Objects;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.AUTH_NOT_FOUND;

@UtilityClass
public class AuthFactory {

    public AuthStrategy create(final InterpreterDependencies dependencies,
                               final String alias,
                               final GlobalTestConfigurationProvider configurationProvider) {
        final Auth auth = getAuthConfig(dependencies.getEnvironment(), alias, configurationProvider);

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

    private Auth getAuthConfig(final String env,
                               final String alias,
                               final GlobalTestConfigurationProvider configurationProvider) {
        List<Api> apiList = IntegrationsUtil.findListByEnv(Api.class, env, configurationProvider);
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

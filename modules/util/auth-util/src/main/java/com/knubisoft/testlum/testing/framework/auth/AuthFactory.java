package com.knubisoft.testlum.testing.framework.auth;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.util.IntegrationsProvider;
import com.knubisoft.testlum.testing.model.global_config.Api;
import com.knubisoft.testlum.testing.model.global_config.Auth;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.NotImplementedException;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;

@UtilityClass
public class AuthFactory {

    private static final String AUTH_NOT_FOUND = "Cannot find <auth> configuration for <api> with alias <%s>";

    public AuthStrategy create(final InterpreterDependencies dependencies, final String alias) {
        IntegrationsProvider integrationsProvider = dependencies.getContext().getBean(IntegrationsProvider.class);
        final Auth auth = getAuthConfig(dependencies.getEnvironment(), alias, integrationsProvider);

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
                               final IntegrationsProvider integrationsProvider) {
        List<Api> apiList = integrationsProvider.findListByEnv(Api.class, env);
        Api apiIntegration = integrationsProvider.findApiForAlias(apiList, alias);
        if (Objects.nonNull(apiIntegration.getAuth())) {
            return apiIntegration.getAuth();
        }
        throw new DefaultFrameworkException(AUTH_NOT_FOUND, apiIntegration.getAlias());
    }

    private AuthStrategy createCustomStrategy(final String className) {
        try {
            return (AuthStrategy) Class.forName(className).getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new NotImplementedException();
        } catch (InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}

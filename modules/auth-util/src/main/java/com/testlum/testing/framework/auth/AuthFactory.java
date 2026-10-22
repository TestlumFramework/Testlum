package com.testlum.testing.framework.auth;

import org.springframework.stereotype.Service;
import com.testlum.testing.framework.env.AliasEnv;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.util.IntegrationsProvider;
import com.testlum.testing.model.global_config.BasicAuthStrategy;
import com.testlum.testing.model.global_config.CustomAuthStrategy;
import com.testlum.testing.model.global_config.JwtAuthStrategy;

import java.lang.reflect.InvocationTargetException;

@Service
public class AuthFactory {

    public AuthStrategy create(final InterpreterDependencies dependencies,
                               final String alias) {
        var configuration = getAuthConfig(dependencies, alias);
        if (configuration instanceof JwtAuthStrategy jwt) {
            return new JwtAuth(dependencies, jwt);
        }
        if (configuration instanceof BasicAuthStrategy basic) {
            return new BasicAuth(dependencies, basic);
        }
        if (configuration instanceof CustomAuthStrategy custom) {
            return createCustomStrategy(dependencies, custom);
        }
        throw new DefaultFrameworkException("Unsupported auth strategy <%s>", configuration.getClass().getName());
    }

    private com.testlum.testing.model.global_config.AuthStrategy getAuthConfig(
            final InterpreterDependencies dependencies, final String alias) {
        IntegrationsProvider integrationsProvider = dependencies.getContext()
                .getBean(IntegrationsProvider.class);
        return integrationsProvider.findAuthStrategy(
                com.testlum.testing.model.global_config.AuthStrategy.class,
                new AliasEnv(alias, dependencies.getEnvironment()));
    }

    private AuthStrategy createCustomStrategy(final InterpreterDependencies dependencies,
                                              final CustomAuthStrategy configuration) {
        try {
            return instantiateCustomStrategy(dependencies, configuration);
        } catch (InvocationTargetException exception) {
            throw new DefaultFrameworkException("Custom auth constructor failed: "
                    + configuration.getClassName(), exception.getTargetException());
        } catch (ReflectiveOperationException | ClassCastException exception) {
            throw new DefaultFrameworkException(
                    "Cannot instantiate custom auth class: " + configuration.getClassName(), exception);
        }
    }

    private AuthStrategy instantiateCustomStrategy(
            final InterpreterDependencies dependencies,
            final CustomAuthStrategy configuration) throws ReflectiveOperationException {
        return Class.forName(configuration.getClassName())
                .asSubclass(AuthStrategy.class)
                .getConstructor(InterpreterDependencies.class, CustomAuthStrategy.class)
                .newInstance(dependencies, configuration);
    }

}

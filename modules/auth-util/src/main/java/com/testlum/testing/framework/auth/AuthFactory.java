package com.testlum.testing.framework.auth;

import org.springframework.stereotype.Service;
import com.testlum.testing.framework.env.AliasEnv;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.util.IntegrationsProvider;
import com.testlum.testing.model.global_config.AuthStrategy;
import com.testlum.testing.model.global_config.BasicAuthStrategy;
import com.testlum.testing.model.global_config.CustomAuthStrategy;
import com.testlum.testing.model.global_config.GoogleAuthStrategy;
import com.testlum.testing.model.global_config.JwtAuthStrategy;
import com.testlum.testing.model.global_config.MicrosoftAuthStrategy;

import java.lang.reflect.InvocationTargetException;

@Service
public class AuthFactory {

    public AuthenticationStrategy create(final InterpreterDependencies dependencies,
                               final String alias) {
        var configuration = getAuthConfig(dependencies, alias);
        if (configuration instanceof JwtAuthStrategy jwt) {
            return new JwtAuth(dependencies, jwt);
        }
        if (configuration instanceof BasicAuthStrategy basic) {
            return new BasicAuth(dependencies, basic);
        }
        return createOAuthOrCustom(dependencies, configuration);
    }

    private AuthenticationStrategy createOAuthOrCustom(final InterpreterDependencies dependencies,
                                            final AuthStrategy configuration) {
        if (configuration instanceof GoogleAuthStrategy google) {
            return new GoogleAuth(dependencies, google);
        }
        if (configuration instanceof MicrosoftAuthStrategy microsoft) {
            return new MicrosoftAuth(dependencies, microsoft);
        }
        return createCustomOrThrow(dependencies, configuration);
    }

    private AuthenticationStrategy createCustomOrThrow(final InterpreterDependencies dependencies,
                                             final AuthStrategy configuration) {
        if (configuration instanceof CustomAuthStrategy custom) {
            return createCustomStrategy(dependencies, custom);
        }
        throw new DefaultFrameworkException("Unsupported auth strategy <%s>", configuration.getClass().getName());
    }

    private AuthStrategy getAuthConfig(
            final InterpreterDependencies dependencies, final String alias) {
        IntegrationsProvider integrationsProvider = dependencies.getContext()
                .getBean(IntegrationsProvider.class);
        return integrationsProvider.findAuthStrategy(
                AuthStrategy.class,
                new AliasEnv(alias, dependencies.getEnvironment()));
    }

    private AuthenticationStrategy createCustomStrategy(final InterpreterDependencies dependencies,
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

    private AuthenticationStrategy instantiateCustomStrategy(
            final InterpreterDependencies dependencies,
            final CustomAuthStrategy configuration) throws ReflectiveOperationException {
        return Class.forName(configuration.getClassName())
                .asSubclass(AuthenticationStrategy.class)
                .getConstructor(InterpreterDependencies.class, CustomAuthStrategy.class)
                .newInstance(dependencies, configuration);
    }

}

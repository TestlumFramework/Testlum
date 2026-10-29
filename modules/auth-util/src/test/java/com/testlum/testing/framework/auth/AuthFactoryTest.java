package com.testlum.testing.framework.auth;

import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.util.IntegrationsProvider;
import com.testlum.testing.model.global_config.BasicAuthStrategy;
import com.testlum.testing.model.global_config.JwtAuthStrategy;
import com.testlum.testing.model.global_config.CustomAuthStrategy;
import com.testlum.testing.framework.env.AliasEnv;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthFactoryTest {

    private AuthFactory authFactory;
    private InterpreterDependencies dependencies;
    private IntegrationsProvider integrationsProvider;
    private ApplicationContext context;

    @BeforeEach
    void setUp() {
        authFactory = new AuthFactory();
        context = mock(ApplicationContext.class);
        integrationsProvider = mock(IntegrationsProvider.class);
        dependencies = mock(InterpreterDependencies.class);

        when(dependencies.getContext()).thenReturn(context);
        when(dependencies.getEnvironment()).thenReturn("test");
        when(context.getBean(IntegrationsProvider.class)).thenReturn(integrationsProvider);
    }

    @Nested
    class Create {
        @Test
        void createsBasicAuthStrategy() {
            mockConfiguration("BASIC", new BasicAuthStrategy());
            assertInstanceOf(BasicAuth.class, authFactory.create(dependencies, "BASIC"));
        }

        @Test
        void createsJwtAuthStrategy() {
            mockConfiguration("JWT", new JwtAuthStrategy());
            assertInstanceOf(JwtAuth.class, authFactory.create(dependencies, "JWT"));
        }

        @Test
        void createsCustomAuthStrategy() {
            CustomAuthStrategy configuration = new CustomAuthStrategy();
            configuration.setClassName(CustomAuthLocal.class.getName());
            mockConfiguration("CUSTOM", configuration);
            assertInstanceOf(CustomAuthLocal.class, authFactory.create(dependencies, "CUSTOM"));
        }

        @Test
        void createsRegressionCustomAuthStrategy() {
            CustomAuthStrategy configuration = new CustomAuthStrategy();
            configuration.setClassName(CustomAuthRegression.class.getName());
            mockConfiguration("CUSTOM", configuration);
            assertInstanceOf(CustomAuthRegression.class, authFactory.create(dependencies, "CUSTOM"));
        }

        @Test
        void defaultAliasUsesConfiguredStrategy() {
            mockConfiguration("DEFAULT", new BasicAuthStrategy());
            assertInstanceOf(BasicAuth.class, authFactory.create(dependencies, "DEFAULT"));
        }

        @Test
        void propagatesMissingStrategyError() {
            when(integrationsProvider.findAuthStrategy(
                    com.testlum.testing.model.global_config.AuthStrategy.class,
                    new AliasEnv("missing", "test")))
                    .thenThrow(new DefaultFrameworkException("Strategy not found"));
            assertThrows(DefaultFrameworkException.class, () -> authFactory.create(dependencies, "missing"));
        }

        @Test
        void rejectsCustomClassWithoutStrategyContract() {
            CustomAuthStrategy configuration = new CustomAuthStrategy();
            configuration.setClassName(String.class.getName());
            mockConfiguration("CUSTOM", configuration);
            assertThrows(DefaultFrameworkException.class, () -> authFactory.create(dependencies, "CUSTOM"));
        }

        @Test
        void rejectsCustomClassWithoutConfigurationConstructor() {
            CustomAuthStrategy configuration = new CustomAuthStrategy();
            configuration.setClassName(DefaultStrategy.class.getName());
            mockConfiguration("CUSTOM", configuration);
            assertThrows(DefaultFrameworkException.class, () -> authFactory.create(dependencies, "CUSTOM"));
        }
    }

    private void mockConfiguration(final String alias,
                                   final com.testlum.testing.model.global_config.AuthStrategy configuration) {
        when(integrationsProvider.findAuthStrategy(
                com.testlum.testing.model.global_config.AuthStrategy.class,
                new AliasEnv(alias, "test"))).thenReturn(configuration);
    }
}

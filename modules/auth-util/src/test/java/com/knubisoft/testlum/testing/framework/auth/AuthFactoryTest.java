package com.knubisoft.testlum.testing.framework.auth;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.util.IntegrationsProvider;
import com.knubisoft.testlum.testing.model.global_config.Api;
import com.knubisoft.testlum.testing.model.global_config.Auth;
import com.knubisoft.testlum.testing.model.global_config.AuthStrategies;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.List;

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
            Api api = buildApiWithStrategy(AuthStrategies.BASIC);
            List<Api> apiList = List.of(api);
            when(integrationsProvider.findListByEnv(Api.class, "test")).thenReturn(apiList);
            when(integrationsProvider.findApiForAlias(apiList, "myAlias")).thenReturn(api);

            AuthStrategy result = authFactory.create(dependencies, "myAlias");

            assertInstanceOf(BasicAuth.class, result);
        }

        @Test
        void createsJwtAuthStrategy() {
            Api api = buildApiWithStrategy(AuthStrategies.JWT);
            List<Api> apiList = List.of(api);
            when(integrationsProvider.findListByEnv(Api.class, "test")).thenReturn(apiList);
            when(integrationsProvider.findApiForAlias(apiList, "myAlias")).thenReturn(api);

            AuthStrategy result = authFactory.create(dependencies, "myAlias");

            assertInstanceOf(JwtAuth.class, result);
        }

        @Test
        void createsDefaultStrategyForDefaultType() {
            Api api = buildApiWithStrategy(AuthStrategies.DEFAULT);
            List<Api> apiList = List.of(api);
            when(integrationsProvider.findListByEnv(Api.class, "test")).thenReturn(apiList);
            when(integrationsProvider.findApiForAlias(apiList, "myAlias")).thenReturn(api);

            AuthStrategy result = authFactory.create(dependencies, "myAlias");

            assertInstanceOf(DefaultStrategy.class, result);
        }

        @Test
        void throwsWhenAuthConfigIsNull() {
            Api api = new Api();
            api.setAlias("myAlias");
            List<Api> apiList = List.of(api);
            when(integrationsProvider.findListByEnv(Api.class, "test")).thenReturn(apiList);
            when(integrationsProvider.findApiForAlias(apiList, "myAlias")).thenReturn(api);

            assertThrows(DefaultFrameworkException.class,
                    () -> authFactory.create(dependencies, "myAlias"));
        }
    }

    private Api buildApiWithStrategy(final AuthStrategies strategyType) {
        Api api = new Api();
        api.setAlias("myAlias");
        Auth auth = new Auth();
        auth.setAuthStrategy(strategyType);
        api.setAuth(auth);
        return api;
    }
}

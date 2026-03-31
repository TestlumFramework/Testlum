package com.knubisoft.testlum.testing.framework.auth;

import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.Auth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthStrategyBaseTest {

    private InterpreterDependencies dependencies;
    private TestAuthStrategy strategy;
    private ArgumentCaptor<InterpreterDependencies.Authorization> authCaptor;

    @BeforeEach
    void setUp() {
        dependencies = mock(InterpreterDependencies.class);
        strategy = new TestAuthStrategy(dependencies);
        authCaptor = ArgumentCaptor.forClass(InterpreterDependencies.Authorization.class);
    }

    @Nested
    class Login {
        @Test
        void setsAuthorizationHeaderWithPrefixAndToken() {
            strategy.callLogin("my-token", "Bearer ");

            verify(dependencies).setAuthorization(authCaptor.capture());
            InterpreterDependencies.Authorization auth = authCaptor.getValue();
            assertEquals("Bearer my-token", auth.getHeaders().get(AuthorizationConstant.HEADER_AUTHORIZATION));
        }

        @Test
        void setsAuthorizationHeaderWithBasicPrefix() {
            strategy.callLogin("encoded-creds", "Basic ");

            verify(dependencies).setAuthorization(authCaptor.capture());
            InterpreterDependencies.Authorization auth = authCaptor.getValue();
            assertEquals("Basic encoded-creds", auth.getHeaders().get(AuthorizationConstant.HEADER_AUTHORIZATION));
        }

        @Test
        void setsAuthorizationHeaderWithEmptyToken() {
            strategy.callLogin("", "Bearer ");

            verify(dependencies).setAuthorization(authCaptor.capture());
            InterpreterDependencies.Authorization auth = authCaptor.getValue();
            assertEquals("Bearer ", auth.getHeaders().get(AuthorizationConstant.HEADER_AUTHORIZATION));
        }
    }

    @Nested
    class Logout {
        @Test
        void setsEmptyHeadersOnLogout() {
            strategy.logout();

            verify(dependencies).setAuthorization(authCaptor.capture());
            InterpreterDependencies.Authorization auth = authCaptor.getValue();
            assertTrue(auth.getHeaders().isEmpty());
        }

        @Test
        void logoutWorksWithoutPriorLogin() {
            strategy.logout();

            verify(dependencies).setAuthorization(authCaptor.capture());
            InterpreterDependencies.Authorization auth = authCaptor.getValue();
            assertEquals(Collections.emptyMap(), auth.getHeaders());
        }
    }

    private static class TestAuthStrategy extends AbstractAuthStrategy {

        TestAuthStrategy(final InterpreterDependencies dependencies) {
            super(dependencies);
        }

        @Override
        public void authenticate(final Auth auth, final CommandResult result) {
            // no-op for testing
        }

        void callLogin(final String token, final String prefix) {
            login(token, prefix);
        }
    }
}

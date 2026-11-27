package com.testlum.testing.framework.scenario;

import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.util.IntegrationsUtil;
import com.testlum.testing.model.global_config.Api;
import com.testlum.testing.model.global_config.Apis;
import com.testlum.testing.model.global_config.Integrations;
import com.testlum.testing.model.scenario.AbstractCommand;
import com.testlum.testing.model.scenario.Auth;
import com.testlum.testing.model.scenario.Logout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AuthCommandExpander} verifying that auth blocks are flattened
 * into their nested commands and an optional automatic logout.
 */
@ExtendWith(MockitoExtension.class)
class AuthCommandExpanderTest {

    private static final String API_ALIAS = "myApi";

    @Mock
    private IntegrationsUtil integrationUtil;
    @Mock
    private Integrations integrations;

    private AuthCommandExpander expander;

    @BeforeEach
    void setUp() {
        expander = new AuthCommandExpander(integrationUtil, integrations);
    }

    @Test
    void authCommandIsExpandedWithLogout() {
        Auth auth = new Auth();
        auth.setApiAlias(API_ALIAS);
        auth.setComment("login");
        auth.getCommands().add(mock(AbstractCommand.class));
        mockApiAuth(true);

        List<AbstractCommand> expanded = expander.expand(List.of(auth));

        assertEquals(3, expanded.size());
        assertInstanceOf(Auth.class, expanded.get(0));
        assertInstanceOf(Logout.class, expanded.get(2));
        assertEquals(API_ALIAS, ((Logout) expanded.get(2)).getAlias());
    }

    @Test
    void authCommandWithNoAutoLogoutSkipsLogout() {
        Auth auth = new Auth();
        auth.setApiAlias(API_ALIAS);
        mockApiAuth(false);

        List<AbstractCommand> expanded = expander.expand(List.of(auth));

        assertEquals(1, expanded.size());
        assertInstanceOf(Auth.class, expanded.get(0));
    }

    @Test
    void expandThrowsWhenApiAuthNotConfigured() {
        Api api = mock(Api.class);
        when(api.getAuth()).thenReturn(null);
        when(api.getAlias()).thenReturn(API_ALIAS);
        mockApiLookup(api);
        Auth auth = new Auth();
        auth.setApiAlias(API_ALIAS);
        List<AbstractCommand> commands = List.of(auth);

        assertThrows(DefaultFrameworkException.class, () -> expander.expand(commands));
    }

    @Test
    void regularCommandIsKeptAsIs() {
        AbstractCommand regularCommand = mock(AbstractCommand.class);

        List<AbstractCommand> expanded = expander.expand(List.of(regularCommand));

        assertEquals(1, expanded.size());
        assertSame(regularCommand, expanded.get(0));
    }

    @Test
    void emptyCommandListStaysEmpty() {
        assertTrue(expander.expand(List.of()).isEmpty());
    }

    private void mockApiAuth(final boolean autoLogout) {
        Api api = mock(Api.class);
        com.testlum.testing.model.global_config.Auth apiAuth =
                mock(com.testlum.testing.model.global_config.Auth.class);
        when(api.getAuth()).thenReturn(apiAuth);
        when(apiAuth.isAutoLogout()).thenReturn(autoLogout);
        mockApiLookup(api);
    }

    private void mockApiLookup(final Api api) {
        Apis apis = mock(Apis.class);
        when(integrations.getApis()).thenReturn(apis);
        when(apis.getApi()).thenReturn(List.of(api));
        when(integrationUtil.findApiForAlias(any(), eq(API_ALIAS))).thenReturn(api);
    }

}

package com.testlum.testing.framework.scenario;

import com.testlum.testing.model.scenario.AbstractCommand;
import com.testlum.testing.model.scenario.Auth;
import com.testlum.testing.model.scenario.Logout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link AuthCommandExpander} verifying that auth blocks are flattened
 * into their nested commands and an optional automatic logout.
 */
class AuthCommandExpanderTest {

    private static final String API_ALIAS = "myApi";


    private AuthCommandExpander expander;

    @BeforeEach
    void setUp() {
        expander = new AuthCommandExpander();
    }

    @Test
    void authCommandIsExpandedWithLogout() {
        Auth auth = new Auth();
        auth.setStrategyAlias(API_ALIAS);
        auth.setComment("login");
        auth.getCommands().add(mock(AbstractCommand.class));
        auth.setAutoLogout(true);

        List<AbstractCommand> expanded = expander.expand(List.of(auth));

        assertEquals(3, expanded.size());
        assertInstanceOf(Auth.class, expanded.get(0));
        assertInstanceOf(Logout.class, expanded.get(2));
        assertEquals(API_ALIAS, ((Logout) expanded.get(2)).getAlias());
    }

    @Test
    void authCommandWithNoAutoLogoutSkipsLogout() {
        Auth auth = new Auth();
        auth.setStrategyAlias(API_ALIAS);
        auth.setAutoLogout(false);

        List<AbstractCommand> expanded = expander.expand(List.of(auth));

        assertEquals(1, expanded.size());
        assertInstanceOf(Auth.class, expanded.get(0));
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

}

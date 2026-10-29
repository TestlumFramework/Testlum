package com.testlum.testing.framework.util;

import com.testlum.testing.framework.env.AliasEnv;
import com.testlum.testing.model.global_config.Integration;
import com.testlum.testing.model.global_config.JwtAuthStrategy;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntegrationsProviderImplTest {

    @Mock
    private IntegrationsUtil integrationsUtil;

    @InjectMocks
    private IntegrationsProviderImpl provider;

    @Nested
    class FindForAliasEnv {
        @Test
        void delegatesToIntegrationsUtil() {
            AliasEnv aliasEnv = mock(AliasEnv.class);
            Integration expected = mock(Integration.class);
            when(integrationsUtil.findForAliasEnv(Integration.class, aliasEnv)).thenReturn(expected);
            Integration actual = provider.findForAliasEnv(Integration.class, aliasEnv);
            assertEquals(expected, actual);
            verify(integrationsUtil).findForAliasEnv(Integration.class, aliasEnv);
        }
    }

    @Nested
    class FindListByEnv {
        @Test
        void delegatesToIntegrationsUtil() {
            List<Integration> expected = List.of(mock(Integration.class));
            when(integrationsUtil.findListByEnv(Integration.class, "test")).thenReturn(expected);
            List<Integration> actual = provider.findListByEnv(Integration.class, "test");
            assertEquals(expected, actual);
        }
    }

    @Nested
    class FindApiForAlias {
        @Test
        void delegatesToIntegrationsUtil() {
            Integration expected = mock(Integration.class);
            List<Integration> integrations = List.of(expected);
            when(integrationsUtil.findApiForAlias(integrations, "alias")).thenReturn(expected);
            Integration actual = provider.findApiForAlias(integrations, "alias");
            assertEquals(expected, actual);
        }
    }

    @Nested
    class IsEnabled {
        @Test
        void alwaysReturnsFalse() {
            List<Integration> integrations = List.of(mock(Integration.class));
            assertFalse(provider.isEnabled(integrations));
        }

        @Test
        void returnsFalseForEmptyList() {
            assertFalse(provider.isEnabled(List.of()));
        }
    }

    @Test
    void delegatesAuthStrategyLookupWithRequestedTypeAndEnvironment() {
        AliasEnv aliasEnv = new AliasEnv("JWT", "ci");
        JwtAuthStrategy expected = new JwtAuthStrategy();
        when(integrationsUtil.findAuthStrategy(JwtAuthStrategy.class, aliasEnv)).thenReturn(expected);
        assertEquals(expected, provider.findAuthStrategy(JwtAuthStrategy.class, aliasEnv));
        verify(integrationsUtil).findAuthStrategy(JwtAuthStrategy.class, aliasEnv);
    }
}

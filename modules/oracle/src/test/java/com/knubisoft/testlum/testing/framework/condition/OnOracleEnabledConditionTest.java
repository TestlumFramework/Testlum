package com.knubisoft.testlum.testing.framework.condition;

import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Oracle;
import com.knubisoft.testlum.testing.model.global_config.OracleIntegration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OnOracleEnabledConditionTest {

    private final OnOracleEnabledCondition condition = new OnOracleEnabledCondition();

    @Nested
    class GetIntegrations {

        @Test
        void returnsOracleListWhenIntegrationsPresent() {
            Integrations integrations = mock(Integrations.class);
            OracleIntegration oracleIntegration = mock(OracleIntegration.class);
            Oracle oracle = mock(Oracle.class);

            when(integrations.getOracleIntegration()).thenReturn(oracleIntegration);
            when(oracleIntegration.getOracle()).thenReturn(List.of(oracle));

            Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertEquals(1, result.get().size());
            assertSame(oracle, result.get().get(0));
        }

        @Test
        void returnsEmptyWhenIntegrationsOptionalIsEmpty() {
            Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.empty());

            assertFalse(result.isPresent());
        }

        @Test
        void returnsEmptyWhenOracleIntegrationIsNull() {
            Integrations integrations = mock(Integrations.class);
            when(integrations.getOracleIntegration()).thenReturn(null);

            Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertFalse(result.isPresent());
        }

        @Test
        void returnsEmptyWhenOracleListIsNull() {
            Integrations integrations = mock(Integrations.class);
            OracleIntegration oracleIntegration = mock(OracleIntegration.class);

            when(integrations.getOracleIntegration()).thenReturn(oracleIntegration);
            when(oracleIntegration.getOracle()).thenReturn(null);

            Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertFalse(result.isPresent());
        }

        @Test
        void returnsMultipleOracleIntegrations() {
            Integrations integrations = mock(Integrations.class);
            OracleIntegration oracleIntegration = mock(OracleIntegration.class);
            Oracle o1 = mock(Oracle.class);
            Oracle o2 = mock(Oracle.class);

            when(integrations.getOracleIntegration()).thenReturn(oracleIntegration);
            when(oracleIntegration.getOracle()).thenReturn(List.of(o1, o2));

            Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertEquals(2, result.get().size());
        }
    }
}

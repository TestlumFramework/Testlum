package com.knubisoft.testlum.testing.framework.condition;

import com.knubisoft.testlum.testing.model.global_config.Clickhouse;
import com.knubisoft.testlum.testing.model.global_config.ClickhouseIntegration;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OnClickhouseEnabledConditionTest {

    private final OnClickhouseEnabledCondition condition = new OnClickhouseEnabledCondition();

    @Nested
    class GetIntegrations {

        @Test
        void returnsClickhouseListWhenIntegrationsPresent() {
            Integrations integrations = mock(Integrations.class);
            ClickhouseIntegration clickhouseIntegration = mock(ClickhouseIntegration.class);
            Clickhouse clickhouse = mock(Clickhouse.class);

            when(integrations.getClickhouseIntegration()).thenReturn(clickhouseIntegration);
            when(clickhouseIntegration.getClickhouse()).thenReturn(List.of(clickhouse));

            Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertEquals(1, result.get().size());
            assertSame(clickhouse, result.get().get(0));
        }

        @Test
        void returnsEmptyWhenIntegrationsOptionalIsEmpty() {
            Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.empty());

            assertFalse(result.isPresent());
        }

        @Test
        void returnsEmptyWhenClickhouseIntegrationIsNull() {
            Integrations integrations = mock(Integrations.class);
            when(integrations.getClickhouseIntegration()).thenReturn(null);

            Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertFalse(result.isPresent());
        }

        @Test
        void returnsEmptyWhenClickhouseListIsNull() {
            Integrations integrations = mock(Integrations.class);
            ClickhouseIntegration clickhouseIntegration = mock(ClickhouseIntegration.class);

            when(integrations.getClickhouseIntegration()).thenReturn(clickhouseIntegration);
            when(clickhouseIntegration.getClickhouse()).thenReturn(null);

            Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertFalse(result.isPresent());
        }

        @Test
        void returnsMultipleClickhouseIntegrations() {
            Integrations integrations = mock(Integrations.class);
            ClickhouseIntegration clickhouseIntegration = mock(ClickhouseIntegration.class);
            Clickhouse ch1 = mock(Clickhouse.class);
            Clickhouse ch2 = mock(Clickhouse.class);

            when(integrations.getClickhouseIntegration()).thenReturn(clickhouseIntegration);
            when(clickhouseIntegration.getClickhouse()).thenReturn(List.of(ch1, ch2));

            Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertEquals(2, result.get().size());
        }
    }
}

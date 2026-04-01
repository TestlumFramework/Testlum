package com.knubisoft.testlum.testing.framework.condition;

import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Postgres;
import com.knubisoft.testlum.testing.model.global_config.PostgresIntegration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OnPostgresEnabledConditionTest {

    private final OnPostgresEnabledCondition condition = new OnPostgresEnabledCondition();

    @Nested
    class GetIntegrations {

        @Test
        void returnsPostgresListWhenIntegrationsPresent() {
            Integrations integrations = mock(Integrations.class);
            PostgresIntegration postgresIntegration = mock(PostgresIntegration.class);
            Postgres postgres = mock(Postgres.class);

            when(integrations.getPostgresIntegration()).thenReturn(postgresIntegration);
            when(postgresIntegration.getPostgres()).thenReturn(List.of(postgres));

            Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertEquals(1, result.get().size());
            assertSame(postgres, result.get().get(0));
        }

        @Test
        void returnsEmptyWhenIntegrationsOptionalIsEmpty() {
            Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.empty());

            assertFalse(result.isPresent());
        }

        @Test
        void returnsEmptyWhenPostgresIntegrationIsNull() {
            Integrations integrations = mock(Integrations.class);
            when(integrations.getPostgresIntegration()).thenReturn(null);

            Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertFalse(result.isPresent());
        }

        @Test
        void returnsEmptyWhenPostgresListIsNull() {
            Integrations integrations = mock(Integrations.class);
            PostgresIntegration postgresIntegration = mock(PostgresIntegration.class);

            when(integrations.getPostgresIntegration()).thenReturn(postgresIntegration);
            when(postgresIntegration.getPostgres()).thenReturn(null);

            Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertFalse(result.isPresent());
        }

        @Test
        void returnsMultiplePostgresIntegrations() {
            Integrations integrations = mock(Integrations.class);
            PostgresIntegration postgresIntegration = mock(PostgresIntegration.class);
            Postgres p1 = mock(Postgres.class);
            Postgres p2 = mock(Postgres.class);

            when(integrations.getPostgresIntegration()).thenReturn(postgresIntegration);
            when(postgresIntegration.getPostgres()).thenReturn(List.of(p1, p2));

            Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertEquals(2, result.get().size());
        }
    }
}

package com.knubisoft.testlum.testing.framework.condition;

import com.knubisoft.testlum.testing.model.global_config.Elasticsearch;
import com.knubisoft.testlum.testing.model.global_config.ElasticsearchIntegration;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Unit tests for {@link OnElasticEnabledCondition}. */
class OnElasticEnabledConditionTest {

    private OnElasticEnabledCondition condition;

    @BeforeEach
    void setUp() {
        condition = new OnElasticEnabledCondition();
    }

    @Nested
    class GetIntegrations {
        @Test
        void returnsElasticsearchListWhenPresent() {
            Integrations integrations = mock(Integrations.class);
            ElasticsearchIntegration esIntegration = mock(ElasticsearchIntegration.class);
            Elasticsearch es = mock(Elasticsearch.class);

            when(integrations.getElasticsearchIntegration()).thenReturn(esIntegration);
            when(esIntegration.getElasticsearch()).thenReturn(List.of(es));

            Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertEquals(1, result.get().size());
        }

        @Test
        void returnsEmptyWhenIntegrationsEmpty() {
            Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.empty());
            assertFalse(result.isPresent());
        }

        @Test
        void returnsEmptyWhenElasticsearchIntegrationIsNull() {
            Integrations integrations = mock(Integrations.class);
            when(integrations.getElasticsearchIntegration()).thenReturn(null);

            Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));
            assertFalse(result.isPresent());
        }

        @Test
        void returnsEmptyListWhenNoElasticsearchConfigs() {
            Integrations integrations = mock(Integrations.class);
            ElasticsearchIntegration esIntegration = mock(ElasticsearchIntegration.class);

            when(integrations.getElasticsearchIntegration()).thenReturn(esIntegration);
            when(esIntegration.getElasticsearch()).thenReturn(Collections.emptyList());

            Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertTrue(result.get().isEmpty());
        }

        @Test
        void returnsNullWhenElasticsearchListIsNull() {
            Integrations integrations = mock(Integrations.class);
            ElasticsearchIntegration esIntegration = mock(ElasticsearchIntegration.class);

            when(integrations.getElasticsearchIntegration()).thenReturn(esIntegration);
            when(esIntegration.getElasticsearch()).thenReturn(null);

            Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));
            assertFalse(result.isPresent());
        }
    }
}

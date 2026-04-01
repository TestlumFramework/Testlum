package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.elasticsearch.ElasticsearchOperation;
import com.knubisoft.testlum.testing.model.global_config.Elasticsearch;
import com.knubisoft.testlum.testing.model.global_config.ElasticsearchIntegration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Unit tests for {@link AliasElasticsearchAdapter}. */
@ExtendWith(MockitoExtension.class)
class AliasElasticsearchAdapterTest {

    @Mock
    private ElasticsearchOperation elasticsearchOperation;

    @Mock
    private Integrations integrations;

    @Mock
    private ElasticsearchIntegration elasticsearchIntegration;

    private AliasElasticsearchAdapter adapter;

    @BeforeEach
    void setUp() {
        when(integrations.getElasticsearchIntegration()).thenReturn(elasticsearchIntegration);
        adapter = new AliasElasticsearchAdapter(elasticsearchOperation, integrations);
    }

    @Nested
    class ApplyMethod {
        @Test
        void registersEnabledElasticsearchInAliasMap() {
            Elasticsearch es = mock(Elasticsearch.class);
            when(es.isEnabled()).thenReturn(true);
            when(es.getAlias()).thenReturn("es-primary");
            when(elasticsearchIntegration.getElasticsearch()).thenReturn(List.of(es));

            Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();
            adapter.apply(aliasMap);

            assertEquals(1, aliasMap.size());
            assertTrue(aliasMap.containsKey("Elasticsearch_es-primary"));
            assertSame(elasticsearchOperation, aliasMap.get("Elasticsearch_es-primary"));
        }

        @Test
        void skipsDisabledElasticsearch() {
            Elasticsearch es = mock(Elasticsearch.class);
            when(es.isEnabled()).thenReturn(false);
            when(elasticsearchIntegration.getElasticsearch()).thenReturn(List.of(es));

            Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();
            adapter.apply(aliasMap);

            assertTrue(aliasMap.isEmpty());
        }

        @Test
        void registersMultipleEnabledElasticsearch() {
            Elasticsearch es1 = mock(Elasticsearch.class);
            when(es1.isEnabled()).thenReturn(true);
            when(es1.getAlias()).thenReturn("alias1");

            Elasticsearch es2 = mock(Elasticsearch.class);
            when(es2.isEnabled()).thenReturn(true);
            when(es2.getAlias()).thenReturn("alias2");

            when(elasticsearchIntegration.getElasticsearch()).thenReturn(List.of(es1, es2));

            Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();
            adapter.apply(aliasMap);

            assertEquals(2, aliasMap.size());
            assertTrue(aliasMap.containsKey("Elasticsearch_alias1"));
            assertTrue(aliasMap.containsKey("Elasticsearch_alias2"));
        }

        @Test
        void mixOfEnabledAndDisabled() {
            Elasticsearch enabled = mock(Elasticsearch.class);
            when(enabled.isEnabled()).thenReturn(true);
            when(enabled.getAlias()).thenReturn("active");

            Elasticsearch disabled = mock(Elasticsearch.class);
            when(disabled.isEnabled()).thenReturn(false);

            when(elasticsearchIntegration.getElasticsearch()).thenReturn(List.of(enabled, disabled));

            Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();
            adapter.apply(aliasMap);

            assertEquals(1, aliasMap.size());
            assertTrue(aliasMap.containsKey("Elasticsearch_active"));
        }
    }

    @Nested
    class StorageName {
        @Test
        void returnsElasticsearch() {
            Elasticsearch es = mock(Elasticsearch.class);
            when(es.isEnabled()).thenReturn(true);
            when(es.getAlias()).thenReturn("test");
            when(elasticsearchIntegration.getElasticsearch()).thenReturn(List.of(es));

            Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();
            adapter.apply(aliasMap);

            assertTrue(aliasMap.keySet().iterator().next().startsWith("Elasticsearch"));
        }
    }
}

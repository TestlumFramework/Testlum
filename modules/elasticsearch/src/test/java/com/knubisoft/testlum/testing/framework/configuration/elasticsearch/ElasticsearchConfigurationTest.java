package com.knubisoft.testlum.testing.framework.configuration.elasticsearch;

import com.knubisoft.testlum.testing.connection.ConnectionTemplate;
import com.knubisoft.testlum.testing.framework.EnvToIntegrationMap;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Elasticsearch;
import com.knubisoft.testlum.testing.model.global_config.ElasticsearchIntegration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import software.amazon.awssdk.http.auth.aws.signer.AwsV4HttpSigner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Unit tests for {@link ElasticsearchConfiguration}. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ElasticsearchConfigurationTest {

    @Mock
    private ConnectionTemplate connectionTemplate;

    private ElasticsearchConfiguration configuration;

    @BeforeEach
    void setUp() {
        configuration = new ElasticsearchConfiguration(connectionTemplate);
    }

    @Nested
    class EsHttpHost {
        @Test
        void createsHttpHostForEnabledElasticsearch() {
            Elasticsearch es = createEnabledElasticsearch("es-alias", "localhost", 9200, "http");
            Map<String, List<Elasticsearch>> esMap = Map.of("dev", List.of(es));

            Map<AliasEnv, HttpHost> result = configuration.esHttpHost(esMap);

            assertEquals(1, result.size());
            AliasEnv key = new AliasEnv("es-alias", "dev");
            assertTrue(result.containsKey(key));
            HttpHost host = result.get(key);
            assertEquals("localhost", host.getHostName());
            assertEquals(9200, host.getPort());
            assertEquals("http", host.getSchemeName());
        }

        @Test
        void skipsDisabledElasticsearch() {
            Elasticsearch es = createDisabledElasticsearch("es-alias");
            Map<String, List<Elasticsearch>> esMap = Map.of("dev", List.of(es));

            Map<AliasEnv, HttpHost> result = configuration.esHttpHost(esMap);

            assertTrue(result.isEmpty());
        }

        @Test
        void handlesMultipleEnvironments() {
            Elasticsearch es1 = createEnabledElasticsearch("es1", "host1", 9200, "http");
            Elasticsearch es2 = createEnabledElasticsearch("es2", "host2", 9201, "https");

            Map<String, List<Elasticsearch>> esMap = new HashMap<>();
            esMap.put("dev", List.of(es1));
            esMap.put("prod", List.of(es2));

            Map<AliasEnv, HttpHost> result = configuration.esHttpHost(esMap);

            assertEquals(2, result.size());
        }

        @Test
        void handlesMultipleEsInSameEnv() {
            Elasticsearch es1 = createEnabledElasticsearch("alias1", "host1", 9200, "http");
            Elasticsearch es2 = createEnabledElasticsearch("alias2", "host2", 9201, "https");

            Map<String, List<Elasticsearch>> esMap = Map.of("dev", List.of(es1, es2));

            Map<AliasEnv, HttpHost> result = configuration.esHttpHost(esMap);

            assertEquals(2, result.size());
        }

        @Test
        void emptyMapReturnsEmptyResult() {
            Map<AliasEnv, HttpHost> result = configuration.esHttpHost(Map.of());
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class RequestConfigCallback {
        @Test
        void createsCallbackForEnabledElasticsearch() {
            Elasticsearch es = createEnabledElasticsearch("alias", "host", 9200, "http");
            when(es.getConnectionTimeout()).thenReturn(5000);
            when(es.getSocketTimeout()).thenReturn(30000);

            Map<String, List<Elasticsearch>> esMap = Map.of("dev", List.of(es));

            Map<AliasEnv, RestClientBuilder.RequestConfigCallback> result =
                    configuration.requestConfigCallback(esMap);

            assertEquals(1, result.size());
            assertNotNull(result.get(new AliasEnv("alias", "dev")));
        }

        @Test
        void skipsDisabledElasticsearch() {
            Elasticsearch es = createDisabledElasticsearch("alias");
            Map<String, List<Elasticsearch>> esMap = Map.of("dev", List.of(es));

            Map<AliasEnv, RestClientBuilder.RequestConfigCallback> result =
                    configuration.requestConfigCallback(esMap);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class HttpClientConfigCallback {
        @Test
        void createsCallbackForEnabledElasticsearch() {
            Elasticsearch es = createEnabledElasticsearch("alias", "host", 9200, "http");
            when(es.getUsername()).thenReturn("user");
            when(es.getPassword()).thenReturn("pass");

            Map<String, List<Elasticsearch>> esMap = Map.of("dev", List.of(es));

            Map<AliasEnv, RestClientBuilder.HttpClientConfigCallback> result =
                    configuration.httpClientConfigCallback(esMap);

            assertEquals(1, result.size());
            assertNotNull(result.get(new AliasEnv("alias", "dev")));
        }

        @Test
        void skipsDisabledElasticsearch() {
            Elasticsearch es = createDisabledElasticsearch("alias");
            Map<String, List<Elasticsearch>> esMap = Map.of("dev", List.of(es));

            Map<AliasEnv, RestClientBuilder.HttpClientConfigCallback> result =
                    configuration.httpClientConfigCallback(esMap);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class RestClientBuilderBean {
        @Test
        void buildsRestClientBuilderForEachEntry() {
            AliasEnv aliasEnv = new AliasEnv("alias", "dev");
            HttpHost httpHost = new HttpHost("localhost", 9200, "http");
            RestClientBuilder.RequestConfigCallback reqCallback = builder -> builder;
            RestClientBuilder.HttpClientConfigCallback httpCallback = builder -> builder;

            Map<AliasEnv, HttpHost> hostMap = Map.of(aliasEnv, httpHost);
            Map<AliasEnv, RestClientBuilder.RequestConfigCallback> reqMap = Map.of(aliasEnv, reqCallback);
            Map<AliasEnv, RestClientBuilder.HttpClientConfigCallback> httpMap = Map.of(aliasEnv, httpCallback);

            Map<AliasEnv, RestClientBuilder> result =
                    configuration.restClientBuilder(hostMap, reqMap, httpMap);

            assertEquals(1, result.size());
            assertNotNull(result.get(aliasEnv));
        }

        @Test
        void emptyMapsReturnEmptyResult() {
            Map<AliasEnv, RestClientBuilder> result =
                    configuration.restClientBuilder(Map.of(), Map.of(), Map.of());
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class Aws4Signer {
        @Test
        void createsSignerForEnabledElasticsearch() {
            Elasticsearch es = createEnabledElasticsearch("alias", "host", 9200, "http");
            Map<String, List<Elasticsearch>> esMap = Map.of("dev", List.of(es));

            Map<AliasEnv, AwsV4HttpSigner> result = configuration.aws4Signer(esMap);

            assertEquals(1, result.size());
            assertNotNull(result.get(new AliasEnv("alias", "dev")));
        }

        @Test
        void skipsDisabledElasticsearch() {
            Elasticsearch es = createDisabledElasticsearch("alias");
            Map<String, List<Elasticsearch>> esMap = Map.of("dev", List.of(es));

            Map<AliasEnv, AwsV4HttpSigner> result = configuration.aws4Signer(esMap);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class GetElasticsearchMap {
        @Test
        void extractsElasticsearchFromEnvToIntegrations() {
            Elasticsearch es = mock(Elasticsearch.class);
            ElasticsearchIntegration esIntegration = mock(ElasticsearchIntegration.class);
            when(esIntegration.getElasticsearch()).thenReturn(List.of(es));

            Integrations integrations = mock(Integrations.class);
            when(integrations.getElasticsearchIntegration()).thenReturn(esIntegration);

            EnvToIntegrationMap envMap = new EnvToIntegrationMap(Map.of("dev", integrations));

            Map<String, List<Elasticsearch>> result = configuration.getElasticsearchMap(envMap);

            assertEquals(1, result.size());
            assertTrue(result.containsKey("dev"));
            assertEquals(1, result.get("dev").size());
        }
    }

    @Nested
    class RestClientBean {
        @Test
        void createsRestClientViaConnectionTemplate() {
            AliasEnv aliasEnv = new AliasEnv("alias", "dev");
            RestClientBuilder builder = mock(RestClientBuilder.class);
            RestClient mockClient = mock(RestClient.class);

            when(builder.build()).thenReturn(mockClient);
            when(connectionTemplate.executeWithRetry(anyString(), any(), any()))
                    .thenAnswer(invocation -> {
                        var supplier = invocation.getArgument(1, java.util.function.Supplier.class);
                        return supplier.get();
                    });

            Map<AliasEnv, RestClientBuilder> builderMap = Map.of(aliasEnv, builder);

            Map<AliasEnv, RestClient> result = configuration.restClient(builderMap);

            assertEquals(1, result.size());
            assertSame(mockClient, result.get(aliasEnv));
        }

        @Test
        void emptyBuilderMapReturnsEmptyResult() {
            Map<AliasEnv, RestClient> result = configuration.restClient(Map.of());
            assertTrue(result.isEmpty());
        }
    }

    private Elasticsearch createEnabledElasticsearch(final String alias, final String host,
                                                        final int port, final String scheme) {
        Elasticsearch es = mock(Elasticsearch.class);
        when(es.isEnabled()).thenReturn(true);
        when(es.getAlias()).thenReturn(alias);
        when(es.getHost()).thenReturn(host);
        when(es.getPort()).thenReturn(port);
        when(es.getScheme()).thenReturn(scheme);
        return es;
    }

    private Elasticsearch createDisabledElasticsearch(final String alias) {
        Elasticsearch es = mock(Elasticsearch.class);
        when(es.isEnabled()).thenReturn(false);
        return es;
    }
}

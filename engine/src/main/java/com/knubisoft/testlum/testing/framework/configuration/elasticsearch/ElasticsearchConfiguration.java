package com.knubisoft.testlum.testing.framework.configuration.elasticsearch;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnElasticEnabledCondition;
import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.connection.ConnectionTemplate;
import com.knubisoft.testlum.testing.framework.configuration.connection.health.HealthCheckFactory;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Elasticsearch;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.http.auth.aws.signer.AwsV4HttpSigner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.CONNECTION_INTEGRATION_DATA;

@Configuration
@Conditional({OnElasticEnabledCondition.class})
@RequiredArgsConstructor
public class ElasticsearchConfiguration {

    @Autowired(required = false)
    private final ConnectionTemplate connectionTemplate;

    private final Map<String, List<Elasticsearch>> elasticsearchMap =
            GlobalTestConfigurationProvider.get().getIntegrations()
                    .entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey,
                            entry -> entry.getValue().getElasticsearchIntegration().getElasticsearch()));

    @Bean(name = "restHighLevelClient")
    public Map<AliasEnv, RestHighLevelClient> restHighLevelClient(
            final Map<AliasEnv, RestClientBuilder> restClientBuilder) {
        Map<AliasEnv, RestHighLevelClient> restHighLevelClientMap = new HashMap<>();
        restClientBuilder.forEach((aliasEnv, clientBuilder) -> {
                    RestHighLevelClient checkedClient = connectionTemplate.executeWithRetry(
                            String.format(CONNECTION_INTEGRATION_DATA, "ElasticSearch HighLevelClient",
                                    aliasEnv.getAlias()),
                            () -> new RestHighLevelClient(clientBuilder),
                            HealthCheckFactory.forElasticRestHighLevelClient()
                    );
                    restHighLevelClientMap.put(aliasEnv, checkedClient);
                });
        return restHighLevelClientMap;
    }

    @Bean(name = "restClient")
    public Map<AliasEnv, RestClient> restClient(final Map<AliasEnv, RestClientBuilder> restClientBuilder) {
        Map<AliasEnv, RestClient> restClientMap = new HashMap<>();
        restClientBuilder.forEach((aliasEnv, clientBuilder) -> {
            RestClient resilientClient = connectionTemplate.executeWithRetry(
                    String.format(CONNECTION_INTEGRATION_DATA, "ElasticSearch Rest Client", aliasEnv.getAlias()),
                    clientBuilder::build,
                    HealthCheckFactory.forElasticRestClient()
            );
            restClientMap.put(aliasEnv, resilientClient);
        });
        return restClientMap;
    }

    @Bean
    public Map<AliasEnv, RestClientBuilder> restClientBuilder(
            final Map<AliasEnv, HttpHost> esHttpHost,
            final Map<AliasEnv, RestClientBuilder.RequestConfigCallback> requestConfigCallback,
            final Map<AliasEnv, RestClientBuilder.HttpClientConfigCallback> httpClientConfigCallback
    ) {
        Map<AliasEnv, RestClientBuilder> clientBuilderMap = new HashMap<>();
        esHttpHost.forEach((aliasEnv, httpHost) -> clientBuilderMap.put(aliasEnv, RestClient.builder(httpHost)
                .setRequestConfigCallback(requestConfigCallback.get(aliasEnv))
                .setHttpClientConfigCallback(httpClientConfigCallback.get(aliasEnv))));
        return clientBuilderMap;
    }

    @Bean
    public Map<AliasEnv, RestClientBuilder.HttpClientConfigCallback> httpClientConfigCallback() {
        final Map<AliasEnv, RestClientBuilder.HttpClientConfigCallback> configCallbackMap = new HashMap<>();
        elasticsearchMap.forEach((env, elasticsearchList) ->
                addClientConfigCallBack(elasticsearchList, env, configCallbackMap));
        return configCallbackMap;
    }

    private void addClientConfigCallBack(final List<Elasticsearch> elasticsearchList,
                                         final String env,
                                         final Map<AliasEnv, RestClientBuilder.HttpClientConfigCallback> configMap) {
        for (Elasticsearch elasticsearch : elasticsearchList) {
            if (elasticsearch.isEnabled()) {
                CredentialsProvider credentialsProvider = createCredentialsProvider(elasticsearch);
                configMap.put(new AliasEnv(elasticsearch.getAlias(), env),
                        builder -> builder.setDefaultCredentialsProvider(credentialsProvider));
            }
        }
    }

    private CredentialsProvider createCredentialsProvider(final Elasticsearch elasticsearch) {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        Credentials credentials = new UsernamePasswordCredentials(elasticsearch.getUsername(),
                elasticsearch.getPassword());
        credentialsProvider.setCredentials(AuthScope.ANY, credentials);
        return credentialsProvider;
    }

    @Bean
    public Map<AliasEnv, RestClientBuilder.RequestConfigCallback> requestConfigCallback() {
        Map<AliasEnv, RestClientBuilder.RequestConfigCallback> configCallbackMap = new HashMap<>();
        elasticsearchMap.forEach((env, elasticsearchList) ->
                addRequestConfigCallback(elasticsearchList, env, configCallbackMap));
        return configCallbackMap;
    }

    private void addRequestConfigCallback(final List<Elasticsearch> elasticsearchList,
                                          final String env,
                                          final Map<AliasEnv, RestClientBuilder.RequestConfigCallback> configMap) {
        for (Elasticsearch elasticsearch : elasticsearchList) {
            if (elasticsearch.isEnabled()) {
                configMap.put(new AliasEnv(elasticsearch.getAlias(), env),
                        builder -> builder.setConnectTimeout(elasticsearch.getConnectionTimeout())
                                .setSocketTimeout(elasticsearch.getSocketTimeout()));
            }
        }
    }

    @Bean
    public Map<AliasEnv, HttpHost> esHttpHost() {
        Map<AliasEnv, HttpHost> httpHostMap = new HashMap<>();
        elasticsearchMap.forEach((env, elasticsearchList) -> addEsHttpHost(elasticsearchList, env, httpHostMap));
        return httpHostMap;
    }

    private void addEsHttpHost(final List<Elasticsearch> elasticsearchList,
                               final String env,
                               final Map<AliasEnv, HttpHost> httpHostMap) {
        for (Elasticsearch elasticsearch : elasticsearchList) {
            if (elasticsearch.isEnabled()) {
                HttpHost httpHost = new HttpHost(elasticsearch.getHost(), elasticsearch.getPort(),
                        elasticsearch.getScheme());
                httpHostMap.put(new AliasEnv(elasticsearch.getAlias(), env), httpHost);
            }
        }
    }

    @Bean
    public Map<AliasEnv, AwsV4HttpSigner> aws4Signer() {
        Map<AliasEnv, AwsV4HttpSigner> signerMap = new HashMap<>();
        elasticsearchMap.forEach((env, elasticsearchList) -> addAWS4Signer(elasticsearchList, env, signerMap));
        return signerMap;
    }

    private void addAWS4Signer(final List<Elasticsearch> elasticsearchList,
                               final String env,
                               final Map<AliasEnv, AwsV4HttpSigner> signerMap) {
        for (Elasticsearch elasticsearch : elasticsearchList) {
            if (elasticsearch.isEnabled()) {
                AwsV4HttpSigner signer = createAWS4Signer(elasticsearch);
                signerMap.put(new AliasEnv(elasticsearch.getAlias(), env), signer);
            }
        }
    }

    private AwsV4HttpSigner createAWS4Signer(final Elasticsearch elasticsearch) {
        return AwsV4HttpSigner.create();
    }
}
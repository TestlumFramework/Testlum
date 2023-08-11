package com.knubisoft.testlum.testing.framework.configuration.elasticsearch;

import com.amazonaws.auth.AWS4Signer;
import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.condition.OnElasticEnabledCondition;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Elasticsearch;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@Conditional({OnElasticEnabledCondition.class})
public class ElasticsearchConfiguration {

    private final Map<String, List<Elasticsearch>> elasticsearchMap;

    public ElasticsearchConfiguration(@Autowired GlobalTestConfigurationProvider globalTestConfigurationProvider) {
        this.elasticsearchMap = globalTestConfigurationProvider.getIntegrations()
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> entry.getValue().getElasticsearchIntegration().getElasticsearch()));
    }

    @Bean(name = "restHighLevelClient")
    public Map<AliasEnv, RestHighLevelClient> restHighLevelClient(
            final Map<AliasEnv, RestClientBuilder> restClientBuilder) {
        Map<AliasEnv, RestHighLevelClient> restHighLevelClientMap = new HashMap<>();
        restClientBuilder.forEach((aliasEnv, clientBuilder) ->
                restHighLevelClientMap.put(aliasEnv, new RestHighLevelClient(clientBuilder)));
        return restHighLevelClientMap;
    }

    @Bean(name = "restClient")
    public Map<AliasEnv, RestClient> restClient(final Map<AliasEnv, RestClientBuilder> restClientBuilder) {
        Map<AliasEnv, RestClient> restClientMap = new HashMap<>();
        restClientBuilder.forEach((aliasEnv, clientBuilder) -> restClientMap.put(aliasEnv, clientBuilder.build()));
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
    public Map<AliasEnv, AWS4Signer> aws4Signer() {
        Map<AliasEnv, AWS4Signer> signerMap = new HashMap<>();
        elasticsearchMap.forEach((env, elasticsearchList) -> addAWS4Signer(elasticsearchList, env, signerMap));
        return signerMap;
    }

    private void addAWS4Signer(final List<Elasticsearch> elasticsearchList,
                               final String env,
                               final Map<AliasEnv, AWS4Signer> signerMap) {
        for (Elasticsearch elasticsearch : elasticsearchList) {
            if (elasticsearch.isEnabled()) {
                AWS4Signer signer = createAWS4Signer(elasticsearch);
                signerMap.put(new AliasEnv(elasticsearch.getAlias(), env), signer);
            }
        }
    }

    private AWS4Signer createAWS4Signer(final Elasticsearch elasticsearch) {
        AWS4Signer signer = new AWS4Signer();
        signer.setServiceName(elasticsearch.getServiceName());
        signer.setRegionName(elasticsearch.getRegion());
        return signer;
    }
}

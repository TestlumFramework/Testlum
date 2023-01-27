package com.knubisoft.cott.testing.framework.configuration.elasticsearch;

import com.amazonaws.auth.AWS4Signer;
import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnElasticEnabledCondition;
import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.model.global_config.Elasticsearch;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.jetbrains.annotations.NotNull;
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
    private final Map<String, List<Elasticsearch>> elasticsearchMap = GlobalTestConfigurationProvider.getIntegrations()
            .entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey,
                    entry -> entry.getValue().getElasticsearchIntegration().getElasticsearch()));

    @Bean
    public Map<String, RestClientBuilder> restClientBuilder(
            final Map<String, HttpHost> esHttpHost,
            final Map<String, RestClientBuilder.RequestConfigCallback>
                    requestConfigCallback,
            final Map<String, RestClientBuilder.HttpClientConfigCallback>
                    httpClientConfigCallback) {
        Map<String, RestClientBuilder> restClientBuilderMap = new HashMap<>();
        esHttpHost.forEach((key, value) -> restClientBuilderMap.put(key, RestClient.builder(value)
                .setRequestConfigCallback(requestConfigCallback.get(key))
                .setHttpClientConfigCallback(httpClientConfigCallback.get(key))));
        return restClientBuilderMap;
    }

    @Bean(name = "restHighLevelClient")
    public Map<String, RestHighLevelClient> restHighLevelClient(
            final Map<String, RestClientBuilder> restClientBuilder) {
        Map<String, RestHighLevelClient> restHighLevelClientMap = new HashMap<>();
        restClientBuilder.forEach((key, value) -> restHighLevelClientMap.put(key, new RestHighLevelClient(value)));
        return restHighLevelClientMap;
    }

    @Bean(name = "restClient")
    public Map<String, RestClient> restClient(final Map<String, RestClientBuilder> restClientBuilder) {
        final Map<String, RestClient> clientMap = new HashMap<>();
        for (Map.Entry<String, RestClientBuilder> entry : restClientBuilder.entrySet()) {
            clientMap.put(entry.getKey(), entry.getValue().build());
        }
        return clientMap;
    }

    @Bean
    public Map<String, AWS4Signer> aws4Signer() {
        Map<String, AWS4Signer> signerMap = new HashMap<>();
        elasticsearchMap.forEach(((s, elasticsearches) -> addAWS4Signer(s, elasticsearches, signerMap)));
        return signerMap;
    }

    private void addAWS4Signer(final String envName,
                               final List<Elasticsearch> elasticsearchList,
                               final Map<String, AWS4Signer> signerMap) {
        for (Elasticsearch elasticsearch : elasticsearchList) {
            if (elasticsearch.isEnabled()) {
                AWS4Signer signer = createAWS4Signer(elasticsearch);
                signerMap.put(envName + DelimiterConstant.UNDERSCORE + elasticsearch.getAlias(), signer);
            }
        }
    }

    @NotNull
    private AWS4Signer createAWS4Signer(final Elasticsearch elasticsearch) {
        AWS4Signer signer = new AWS4Signer();
        signer.setServiceName(elasticsearch.getServiceName());
        signer.setRegionName(elasticsearch.getRegion());
        return signer;
    }

    @Bean
    public Map<String, RestClientBuilder.HttpClientConfigCallback> httpClientConfigCallback() {
        final Map<String, RestClientBuilder.HttpClientConfigCallback> configCallbackMap = new HashMap<>();
        elasticsearchMap.forEach(((s, elasticsearchList) ->
                addClientConfigCallBack(s, elasticsearchList, configCallbackMap)));
        return configCallbackMap;
    }

    private void addClientConfigCallBack(final String envName,
                                         final List<Elasticsearch> elasticsearchList,
                                         final Map<String, RestClientBuilder.HttpClientConfigCallback> configCallbackMap) {
        for (Elasticsearch elasticsearch : elasticsearchList) {
            if (elasticsearch.isEnabled()) {
                createClientConfigCallBackAndPutIntoMap(configCallbackMap, elasticsearch, envName);
            }
        }
    }

    private void createClientConfigCallBackAndPutIntoMap(
            final Map<String, RestClientBuilder.HttpClientConfigCallback> configCallbackMap,
            final Elasticsearch elasticsearch,
            final String envName) {
        final CredentialsProvider credentialsProvider = createCredentialsProvider(elasticsearch);
        configCallbackMap.put(envName + DelimiterConstant.UNDERSCORE + elasticsearch.getAlias(),
                httpAsyncClientBuilder -> httpAsyncClientBuilder
                        .setDefaultCredentialsProvider(credentialsProvider));
    }

    @NotNull
    private CredentialsProvider createCredentialsProvider(final Elasticsearch elasticsearch) {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        Credentials credentials = new UsernamePasswordCredentials(elasticsearch.getUsername(),
                elasticsearch.getPassword());
        credentialsProvider.setCredentials(AuthScope.ANY, credentials);
        return credentialsProvider;
    }

    @Bean
    public Map<String, RestClientBuilder.RequestConfigCallback> requestConfigCallback() {
        Map<String, RestClientBuilder.RequestConfigCallback> configCallbackMap = new HashMap<>();
        elasticsearchMap.forEach(((s, elasticsearchList) ->
                addRequestConfigCallback(s, elasticsearchList, configCallbackMap)));
        return configCallbackMap;
    }

    private void addRequestConfigCallback(final String envName,
                                          final List<Elasticsearch> elasticsearchList,
                                          final Map<String, RestClientBuilder.RequestConfigCallback> configCallbackMap) {
        for (Elasticsearch elasticsearch : elasticsearchList) {
            if (elasticsearch.isEnabled()) {
                addToRequestConfigMap(configCallbackMap, elasticsearch, envName);
            }
        }
    }

    private void addToRequestConfigMap(final Map<String, RestClientBuilder.RequestConfigCallback> configCallbackMap,
                                          final Elasticsearch elasticsearch,
                                          final String envName) {
        configCallbackMap.put(envName + DelimiterConstant.UNDERSCORE + elasticsearch.getAlias(), builder -> builder
                .setConnectTimeout(elasticsearch.getConnectionTimeout())
                .setSocketTimeout(elasticsearch.getSocketTimeout()));
    }

    @Bean
    public Map<String, HttpHost> esHttpHost() {
        Map<String, HttpHost> hostMap = new HashMap<>();
        elasticsearchMap.forEach(((s, elasticsearchList) -> addEsHttpHost(s, elasticsearchList, hostMap)));
        return hostMap;
    }

    private void addEsHttpHost(final String envName,
                               final List<Elasticsearch> elasticsearchList,
                               final Map<String, HttpHost> hostMap) {
        for (Elasticsearch elasticsearch : elasticsearchList) {
            if (elasticsearch.isEnabled()) {
                hostMap.put(envName + DelimiterConstant.UNDERSCORE +elasticsearch.getAlias(),
                        new HttpHost(elasticsearch.getHost(),
                        elasticsearch.getPort(), elasticsearch.getScheme()));
            }
        }
    }
}

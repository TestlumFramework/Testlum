package com.knubisoft.cott.testing.framework.configuration.dynamo;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnDynamoEnabledCondition;
import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.model.global_config.Dynamo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@Conditional({OnDynamoEnabledCondition.class})
public class DynamoDBConfiguration {

    private final Map<String, List<Dynamo>> dynamoMap = GlobalTestConfigurationProvider.getIntegrations()
            .entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey,
                    entry -> entry.getValue().getDynamoIntegration().getDynamo()));

    @Bean
    public Map<String, DynamoDbClient> dynamodb() {
        Map<String, DynamoDbClient> dbClientMap = new HashMap<>();
        dynamoMap.forEach(((s, dynamoList) -> addDynamoClient(s, dynamoList, dbClientMap)));
        return dbClientMap;
    }

    private void addDynamoClient(final String envName,
                                 final List<Dynamo> dynamoList,
                                 final Map<String, DynamoDbClient> dbClientMap) {
        for (Dynamo dynamo : dynamoList) {
            if (dynamo.isEnabled()) {
                createDynamoClientAndPutIntoMap(dbClientMap, dynamo, envName);
            }
        }
    }

    private void createDynamoClientAndPutIntoMap(final Map<String, DynamoDbClient> dbClientMap,
                                                 final Dynamo dynamo,
                                                 final String envName) {
        URI uri = URI.create(dynamo.getEndpoint());
        Region region = Region.of(dynamo.getRegion());
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(dynamo.getAccessKeyId(),
                dynamo.getSecretAccessKey());
        StaticCredentialsProvider staticCredentialsProvider = StaticCredentialsProvider
                .create(awsBasicCredentials);
        dbClientMap.put(envName + DelimiterConstant.UNDERSCORE + dynamo.getAlias(),
                buildDynamoDbClient(uri, region, staticCredentialsProvider));
    }

    private DynamoDbClient buildDynamoDbClient(final URI uri,
                                               final Region region,
                                               final StaticCredentialsProvider staticCredentialsProvider) {
        return DynamoDbClient.builder()
                .endpointOverride(uri)
                .region(region)
                .credentialsProvider(staticCredentialsProvider)
                .build();
    }

    @Bean
    public Map<String, AmazonDynamoDB> amazonDynamoDB() {
        Map<String, AmazonDynamoDB> dynamoDBMap = new HashMap<>();
        dynamoMap.forEach(((s, dynamoList) -> addDynamoDB(s, dynamoList, dynamoDBMap)));
        return dynamoDBMap;
    }

    private void addDynamoDB(final String envName,
                             final List<Dynamo> dynamoList,
                             final Map<String, AmazonDynamoDB> dynamoDBMap) {
        for (Dynamo dynamo : dynamoList) {
            if (dynamo.isEnabled()) {
                createDynamoDBAndPutIntoMap(dynamoDBMap, dynamo, envName);
            }
        }
    }

    private void createDynamoDBAndPutIntoMap(final Map<String, AmazonDynamoDB> dynamoDBMap,
                                             final Dynamo dynamo,
                                             final String envName) {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(dynamo.getAccessKeyId(),
                dynamo.getSecretAccessKey());
        AwsClientBuilder.EndpointConfiguration endpointConfiguration =
                new AwsClientBuilder.EndpointConfiguration(dynamo.getEndpoint(), dynamo.getRegion());
        AWSStaticCredentialsProvider awsStaticCredentialsProvider =
                new AWSStaticCredentialsProvider(awsCredentials);
        dynamoDBMap.put(envName + DelimiterConstant.UNDERSCORE + dynamo.getAlias(),
                buildAmazonDynamoDB(endpointConfiguration,
                awsStaticCredentialsProvider));
    }

    private AmazonDynamoDB buildAmazonDynamoDB(final AwsClientBuilder.EndpointConfiguration endpointConfiguration,
                                               final AWSStaticCredentialsProvider awsStaticCredentialsProvider) {
        return AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(endpointConfiguration)
                .withCredentials(awsStaticCredentialsProvider)
                .build();
    }

}

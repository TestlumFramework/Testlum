package com.knubisoft.e2e.testing.framework.configuration.dynamo;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.configuration.condition.OnDynamoEnabledCondition;
import com.knubisoft.e2e.testing.model.global_config.Dynamo;
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

@Configuration
@Conditional({OnDynamoEnabledCondition.class})
public class DynamoDBConfiguration {
    private final List<Dynamo> dynamoList =
            GlobalTestConfigurationProvider.getIntegrations().getDynamoIntegration().getDynamo();

    @Bean
    public Map<String, DynamoDbClient> dynamodb() {
        Map<String, DynamoDbClient> dbClientMap = new HashMap<>();
        for (Dynamo dynamo : dynamoList) {
            if (dynamo.isEnabled()) {
                createDynamoClientAndPutIntoMap(dbClientMap, dynamo);
            }
        }
        return dbClientMap;
    }

    private void createDynamoClientAndPutIntoMap(final Map<String, DynamoDbClient> dbClientMap, final Dynamo dynamo) {
        URI uri = URI.create(dynamo.getEndpoint());
        Region region = Region.of(dynamo.getRegion());
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(dynamo.getAccessKeyId(),
                dynamo.getSecretAccessKey());
        StaticCredentialsProvider staticCredentialsProvider = StaticCredentialsProvider
                .create(awsBasicCredentials);
        dbClientMap.put(dynamo.getAlias(), buildDynamoDbClient(uri, region, staticCredentialsProvider));
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
        for (Dynamo dynamo : dynamoList) {
            if (dynamo.isEnabled()) {
                createDynamoDBAndPutIntoMap(dynamoDBMap, dynamo);
            }
        }
        return dynamoDBMap;
    }

    private void createDynamoDBAndPutIntoMap(final Map<String, AmazonDynamoDB> dynamoDBMap, final Dynamo dynamo) {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(dynamo.getAccessKeyId(),
                dynamo.getSecretAccessKey());
        AwsClientBuilder.EndpointConfiguration endpointConfiguration =
                new AwsClientBuilder.EndpointConfiguration(dynamo.getEndpoint(), dynamo.getRegion());
        AWSStaticCredentialsProvider awsStaticCredentialsProvider =
                new AWSStaticCredentialsProvider(awsCredentials);
        dynamoDBMap.put(dynamo.getAlias(), buildAmazonDynamoDB(endpointConfiguration,
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

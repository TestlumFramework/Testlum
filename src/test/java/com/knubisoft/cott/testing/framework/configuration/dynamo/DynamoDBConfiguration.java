package com.knubisoft.cott.testing.framework.configuration.dynamo;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnDynamoEnabledCondition;
import com.knubisoft.cott.testing.model.AliasEnv;
import com.knubisoft.cott.testing.model.global_config.Dynamo;
import com.knubisoft.cott.testing.model.global_config.Integrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Conditional({OnDynamoEnabledCondition.class})
public class DynamoDBConfiguration {

    @Bean
    public Map<AliasEnv, DynamoDbClient> dynamodb() {
        Map<AliasEnv, DynamoDbClient> dbClientMap = new HashMap<>();
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach((env, integrations) -> addDynamoDbClient(integrations, env, dbClientMap));
        return dbClientMap;
    }

    private void addDynamoDbClient(final Integrations integrations,
                                   final String env,
                                   final Map<AliasEnv, DynamoDbClient> dbClientMap) {
        for (Dynamo dynamo : integrations.getDynamoIntegration().getDynamo()) {
            if (dynamo.isEnabled()) {
                dbClientMap.put(new AliasEnv(dynamo.getAlias(), env), createDynamoDbClient(dynamo));
            }
        }
    }

    private DynamoDbClient createDynamoDbClient(final Dynamo dynamo) {
        URI uri = URI.create(dynamo.getEndpoint());
        Region region = Region.of(dynamo.getRegion());
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(dynamo.getAccessKeyId(),
                dynamo.getSecretAccessKey());
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(awsBasicCredentials);
        return buildDynamoDbClient(uri, region, credentialsProvider);
    }

    private DynamoDbClient buildDynamoDbClient(final URI uri,
                                               final Region region,
                                               final AwsCredentialsProvider awsCredentialsProvider) {
        return DynamoDbClient.builder()
                .endpointOverride(uri)
                .region(region)
                .credentialsProvider(awsCredentialsProvider)
                .build();
    }
}

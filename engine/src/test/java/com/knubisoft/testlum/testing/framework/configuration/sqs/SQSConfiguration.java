package com.knubisoft.testlum.testing.framework.configuration.sqs;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnSQSEnabledCondition;
import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Sqs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Conditional({OnSQSEnabledCondition.class})
public class SQSConfiguration {

    @Bean
    public Map<AliasEnv, SqsClient> sqsClient() {
        final Map<AliasEnv, SqsClient> amazonSqsMap = new HashMap<>();
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach((env, integrations) -> addAmazonSqs(integrations, env, amazonSqsMap));
        return amazonSqsMap;
    }

    private void addAmazonSqs(final Integrations integrations,
                              final String env,
                              final Map<AliasEnv, SqsClient> amazonSqsMap) {
        for (Sqs sqs : integrations.getSqsIntegration().getSqs()) {
            if (sqs.isEnabled()) {
                SqsClient amazonSqs = createAmazonSqs(sqs);
                amazonSqsMap.put(new AliasEnv(sqs.getAlias(), env), amazonSqs);
            }
        }
    }

    private SqsClient createAmazonSqs(final Sqs sqs) {
        AwsBasicCredentials basicAWSCredentials =
                AwsBasicCredentials.create(sqs.getAccessKeyId(), sqs.getSecretAccessKey());
        StaticCredentialsProvider awsStaticCredentialsProvider = StaticCredentialsProvider.create(basicAWSCredentials);
        return buildAmazonSQS(sqs, awsStaticCredentialsProvider);
    }

    private SqsClient buildAmazonSQS(final Sqs sqs,
                                     final StaticCredentialsProvider awsStaticCredentialsProvider) {
        return SqsClient.builder()
                .region(Region.of(sqs.getRegion()))
                .credentialsProvider(awsStaticCredentialsProvider)
                .endpointOverride(URI.create(sqs.getEndpoint()))
                .build();
    }
}
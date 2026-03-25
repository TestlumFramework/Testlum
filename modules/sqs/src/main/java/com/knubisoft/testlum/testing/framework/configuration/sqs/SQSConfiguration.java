package com.knubisoft.testlum.testing.framework.configuration.sqs;

import com.knubisoft.testlum.testing.connection.ConnectionTemplate;
import com.knubisoft.testlum.testing.connection.IntegrationHealthCheck;
import com.knubisoft.testlum.testing.framework.EnvToIntegrationMap;
import com.knubisoft.testlum.testing.framework.condition.OnSQSEnabledCondition;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Sqs;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class SQSConfiguration {

    private final ConnectionTemplate connectionTemplate;

    @Bean
    public Map<AliasEnv, SqsClient> sqsClient(final EnvToIntegrationMap envTointegrations) {
        final Map<AliasEnv, SqsClient> amazonSqsMap = new HashMap<>();
        envTointegrations
                .forEach((env, integrations) -> addAmazonSqs(integrations, env, amazonSqsMap));
        return amazonSqsMap;
    }

    private void addAmazonSqs(final Integrations integrations,
                              final String env,
                              final Map<AliasEnv, SqsClient> amazonSqsMap) {
        for (Sqs sqs : integrations.getSqsIntegration().getSqs()) {
            if (sqs.isEnabled()) {
                SqsClient checkedSqsClient = connectionTemplate.executeWithRetry(
                        String.format(LogMessage.CONNECTION_INTEGRATION_DATA, "SQS", sqs.getAlias()),
                        () -> createAmazonSqs(sqs),
                        forSqs()
                );

                amazonSqsMap.put(new AliasEnv(sqs.getAlias(), env), checkedSqsClient);
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

    private IntegrationHealthCheck<SqsClient> forSqs() {
        return sqsClient -> sqsClient.listQueues(lq -> lq.maxResults(1));
    }
}
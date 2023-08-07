package com.knubisoft.testlum.testing.framework.configuration.sqs;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.condition.OnSQSEnabledCondition;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Sqs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Conditional({OnSQSEnabledCondition.class})
public class SQSConfiguration {

    @Autowired
    private GlobalTestConfigurationProvider globalTestConfigurationProvider;

    @Bean
    public Map<AliasEnv, AmazonSQS> amazonSQS() {
        final Map<AliasEnv, AmazonSQS> amazonSqsMap = new HashMap<>();
        globalTestConfigurationProvider.getIntegrations()
                .forEach((env, integrations) -> addAmazonSqs(integrations, env, amazonSqsMap));
        return amazonSqsMap;
    }

    private void addAmazonSqs(final Integrations integrations,
                              final String env,
                              final Map<AliasEnv, AmazonSQS> amazonSqsMap) {
        for (Sqs sqs : integrations.getSqsIntegration().getSqs()) {
            if (sqs.isEnabled()) {
                AmazonSQS amazonSqs = createAmazonSqs(sqs);
                amazonSqsMap.put(new AliasEnv(sqs.getAlias(), env), amazonSqs);
            }
        }
    }

    private AmazonSQS createAmazonSqs(final Sqs sqs) {
        AwsClientBuilder.EndpointConfiguration endpointConfiguration =
                new AwsClientBuilder.EndpointConfiguration(sqs.getEndpoint(), sqs.getRegion());
        BasicAWSCredentials basicAWSCredentials =
                new BasicAWSCredentials(sqs.getAccessKeyId(), sqs.getSecretAccessKey());
        AWSStaticCredentialsProvider awsStaticCredentialsProvider =
                new AWSStaticCredentialsProvider(basicAWSCredentials);
        return buildAmazonSQS(endpointConfiguration, awsStaticCredentialsProvider);
    }

    private AmazonSQS buildAmazonSQS(final AwsClientBuilder.EndpointConfiguration endpointConfiguration,
                                     final AWSStaticCredentialsProvider awsStaticCredentialsProvider) {
        return AmazonSQSClientBuilder.standard()
                .withEndpointConfiguration(endpointConfiguration)
                .withCredentials(awsStaticCredentialsProvider)
                .build();
    }
}

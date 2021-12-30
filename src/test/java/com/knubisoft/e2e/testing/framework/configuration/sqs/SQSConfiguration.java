package com.knubisoft.e2e.testing.framework.configuration.sqs;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.configuration.condition.OnSQSEnabledCondition;
import com.knubisoft.e2e.testing.model.global_config.Sqs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Conditional({OnSQSEnabledCondition.class})
public class SQSConfiguration {

    @Bean
    public Map<String, AmazonSQS> amazonSQS() {
        final Map<String, AmazonSQS> properties = new HashMap<>();
        for (Sqs sqs : GlobalTestConfigurationProvider.provide().getSqss().getSqs()) {
            if (sqs.isEnabled()) {
                createSqsAndPutIntoMap(properties, sqs);
            }
        }
        return properties;
    }

    private void createSqsAndPutIntoMap(final Map<String, AmazonSQS> properties, final Sqs sqs) {
        AwsClientBuilder.EndpointConfiguration endpointConfiguration =
                new AwsClientBuilder.EndpointConfiguration(sqs.getEndpoint(), sqs.getRegion());
        BasicAWSCredentials basicAWSCredentials =
                new BasicAWSCredentials(sqs.getAccessKeyId(), sqs.getSecretAccessKey());
        AWSStaticCredentialsProvider awsStaticCredentialsProvider =
                new AWSStaticCredentialsProvider(basicAWSCredentials);
        properties.put(sqs.getAlias(), buildAmazonSQS(endpointConfiguration, awsStaticCredentialsProvider));
    }

    private AmazonSQS buildAmazonSQS(final AwsClientBuilder.EndpointConfiguration endpointConfiguration,
                                     final AWSStaticCredentialsProvider awsStaticCredentialsProvider) {
        return AmazonSQSClientBuilder.standard()
                .withEndpointConfiguration(endpointConfiguration)
                .withCredentials(awsStaticCredentialsProvider)
                .build();
    }

}

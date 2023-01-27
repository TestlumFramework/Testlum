package com.knubisoft.cott.testing.framework.configuration.sqs;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnSQSEnabledCondition;
import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.model.global_config.Sqs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@Conditional({OnSQSEnabledCondition.class})
public class SQSConfiguration {

    @Bean
    public Map<String, AmazonSQS> amazonSQS() {
        final Map<String, AmazonSQS> sqsIntegration = new HashMap<>();
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach(((s, integrations) -> addSqs(s, integrations.getSqsIntegration().getSqs(), sqsIntegration)));
        return sqsIntegration;
    }

    private void addSqs(final String envName, final List<Sqs> sqsList, final Map<String, AmazonSQS> sqsIntegration) {
        for (Sqs sqs : sqsList) {
            if (sqs.isEnabled()) {
                createSqsAndPutIntoMap(sqsIntegration, sqs, envName);
            }
        }
    }

    private void createSqsAndPutIntoMap(final Map<String, AmazonSQS> properties, final Sqs sqs, final String envName) {
        AwsClientBuilder.EndpointConfiguration endpointConfiguration =
                new AwsClientBuilder.EndpointConfiguration(sqs.getEndpoint(), sqs.getRegion());
        BasicAWSCredentials basicAWSCredentials =
                new BasicAWSCredentials(sqs.getAccessKeyId(), sqs.getSecretAccessKey());
        AWSStaticCredentialsProvider awsStaticCredentialsProvider =
                new AWSStaticCredentialsProvider(basicAWSCredentials);
        properties.put(envName + DelimiterConstant.UNDERSCORE + sqs.getAlias(),
                buildAmazonSQS(endpointConfiguration, awsStaticCredentialsProvider));
    }

    private AmazonSQS buildAmazonSQS(final AwsClientBuilder.EndpointConfiguration endpointConfiguration,
                                     final AWSStaticCredentialsProvider awsStaticCredentialsProvider) {
        return AmazonSQSClientBuilder.standard()
                .withEndpointConfiguration(endpointConfiguration)
                .withCredentials(awsStaticCredentialsProvider)
                .build();
    }

}

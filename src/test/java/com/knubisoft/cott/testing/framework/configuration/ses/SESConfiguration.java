package com.knubisoft.cott.testing.framework.configuration.ses;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnSESEnabledCondition;
import com.knubisoft.cott.testing.model.global_config.Ses;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@Conditional({OnSESEnabledCondition.class})
public class SESConfiguration {
    private final List<Ses> sesList = GlobalTestConfigurationProvider.getIntegrations().getSesIntegration().getSes();

    @Bean
    public Map<String, AmazonSimpleEmailService> amazonSimpleEmailService() {
        Map<String, AmazonSimpleEmailService> emailServiceMap = new HashMap<>();
        for (Ses ses : sesList) {
            if (ses.isEnabled()) {
                createSesAndPutIntoMap(emailServiceMap, ses);
            }
        }
        return emailServiceMap;
    }

    private void createSesAndPutIntoMap(final Map<String, AmazonSimpleEmailService> emailServiceMap, final Ses ses) {
        AwsClientBuilder.EndpointConfiguration endpointConfiguration =
                new AwsClientBuilder.EndpointConfiguration(ses.getEndpoint(), ses.getRegion());
        BasicAWSCredentials basicAWSCredentials =
                new BasicAWSCredentials(ses.getAccessKeyId(), ses.getSecretAccessKey());
        AWSStaticCredentialsProvider awsStaticCredentialsProvider =
                new AWSStaticCredentialsProvider(basicAWSCredentials);
        emailServiceMap.put(ses.getAlias(), buildAmazonSes(endpointConfiguration,
                awsStaticCredentialsProvider));
    }

    private AmazonSimpleEmailService buildAmazonSes(final AwsClientBuilder.EndpointConfiguration endpointConfiguration,
                                                    final AWSStaticCredentialsProvider credentialsProvider) {
        return AmazonSimpleEmailServiceClientBuilder.standard()
                .withEndpointConfiguration(endpointConfiguration)
                .withCredentials(credentialsProvider)
                .build();
    }

}

package com.knubisoft.testlum.testing.framework.configuration.ses;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.condition.OnSESEnabledCondition;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Ses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Conditional({OnSESEnabledCondition.class})
public class SESConfiguration {

    @Autowired
    private GlobalTestConfigurationProvider globalTestConfigurationProvider;

    @Bean
    public Map<AliasEnv, AmazonSimpleEmailService> amazonSimpleEmailService() {
        Map<AliasEnv, AmazonSimpleEmailService> amazonSesMap = new HashMap<>();
        globalTestConfigurationProvider.getIntegrations()
                .forEach((env, integrations) -> addAmazonSes(integrations, env, amazonSesMap));
        return amazonSesMap;
    }

    private void addAmazonSes(final Integrations integrations,
                              final String env,
                              final Map<AliasEnv, AmazonSimpleEmailService> emailServiceMap) {
        for (Ses ses : integrations.getSesIntegration().getSes()) {
            if (ses.isEnabled()) {
                AmazonSimpleEmailService amazonSes = createAmazonSes(ses);
                emailServiceMap.put(new AliasEnv(ses.getAlias(), env), amazonSes);
            }
        }
    }

    private AmazonSimpleEmailService createAmazonSes(final Ses ses) {
        AwsClientBuilder.EndpointConfiguration endpointConfiguration =
                new AwsClientBuilder.EndpointConfiguration(ses.getEndpoint(), ses.getRegion());
        BasicAWSCredentials basicAWSCredentials =
                new BasicAWSCredentials(ses.getAccessKeyId(), ses.getSecretAccessKey());
        AWSStaticCredentialsProvider awsStaticCredentialsProvider =
                new AWSStaticCredentialsProvider(basicAWSCredentials);
        return buildAmazonSes(endpointConfiguration, awsStaticCredentialsProvider);
    }

    private AmazonSimpleEmailService buildAmazonSes(final AwsClientBuilder.EndpointConfiguration endpointConfiguration,
                                                    final AWSStaticCredentialsProvider credentialsProvider) {
        return AmazonSimpleEmailServiceClientBuilder.standard()
                .withEndpointConfiguration(endpointConfiguration)
                .withCredentials(credentialsProvider)
                .build();
    }
}

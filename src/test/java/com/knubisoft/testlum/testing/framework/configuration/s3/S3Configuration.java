package com.knubisoft.testlum.testing.framework.configuration.s3;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.condition.OnS3EnabledCondition;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.S3;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Conditional({OnS3EnabledCondition.class})
public class S3Configuration {

    @Bean
    public Map<AliasEnv, AmazonS3> amazonS3() {
        Map<AliasEnv, AmazonS3> amazonS3Map = new HashMap<>();
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach((env, integrations) -> addAmazonS3(integrations, env, amazonS3Map));
        return amazonS3Map;
    }

    private void addAmazonS3(final Integrations integrations,
                             final String env,
                             final Map<AliasEnv, AmazonS3> amazonS3Map) {
        for (S3 s3 : integrations.getS3Integration().getS3()) {
            if (s3.isEnabled()) {
                amazonS3Map.put(new AliasEnv(s3.getAlias(), env), createAmazonS3(s3));
            }
        }
    }

    private AmazonS3 createAmazonS3(final S3 s3) {
        AwsClientBuilder.EndpointConfiguration endpointConfiguration =
                new AwsClientBuilder.EndpointConfiguration(s3.getEndpoint(), s3.getRegion());
        BasicAWSCredentials basicAWSCredentials =
                new BasicAWSCredentials(s3.getAccessKeyId(), s3.getSecretAccessKey());
        AWSStaticCredentialsProvider awsStaticCredentialsProvider =
                new AWSStaticCredentialsProvider(basicAWSCredentials);
        return buildAmazonS3(endpointConfiguration, awsStaticCredentialsProvider);
    }

    private AmazonS3 buildAmazonS3(final AwsClientBuilder.EndpointConfiguration endpointConfiguration,
                                   final AWSStaticCredentialsProvider awsStaticCredentialsProvider) {
        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(endpointConfiguration)
                .withCredentials(awsStaticCredentialsProvider)
                .withPathStyleAccessEnabled(true)
                .build();
    }
}

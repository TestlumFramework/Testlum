package com.knubisoft.e2e.testing.framework.configuration.s3;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.configuration.condition.OnS3EnabledCondition;
import com.knubisoft.e2e.testing.model.global_config.S3;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Conditional({OnS3EnabledCondition.class})
public class S3Configuration {

    @Bean
    public Map<String, AmazonS3> amazonS3() {
        Map<String, AmazonS3> s3Integration = new HashMap<>();
        for (S3 s3 : GlobalTestConfigurationProvider.getIntegrations().getS3Integration().getS3()) {
            if (s3.isEnabled()) {
                createS3AndPutToMap(s3Integration, s3);
            }
        }
        return s3Integration;
    }

    private void createS3AndPutToMap(final Map<String, AmazonS3> s3Map, final S3 s3) {
        AwsClientBuilder.EndpointConfiguration endpointConfiguration =
                new AwsClientBuilder.EndpointConfiguration(s3.getEndpoint(), s3.getRegion());
        BasicAWSCredentials basicAWSCredentials =
                new BasicAWSCredentials(s3.getAccessKeyId(), s3.getSecretAccessKey());
        AWSStaticCredentialsProvider awsStaticCredentialsProvider =
                new AWSStaticCredentialsProvider(basicAWSCredentials);
        s3Map.put(s3.getAlias(), buildAmazonS3(endpointConfiguration, awsStaticCredentialsProvider));
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

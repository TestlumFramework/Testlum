package com.knubisoft.testlum.testing.framework.configuration.s3;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnS3EnabledCondition;
import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.connection.ConnectionTemplate;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.S3;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Conditional({OnS3EnabledCondition.class})
@RequiredArgsConstructor
public class S3Configuration {

    private final ConnectionTemplate connectionTemplate;

    @Bean
    public Map<AliasEnv, S3Client> s3Client() {
        Map<AliasEnv, S3Client> amazonS3Map = new HashMap<>();
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach((env, integrations) -> addAmazonS3(integrations, env, amazonS3Map));
        return amazonS3Map;
    }

    private void addAmazonS3(final Integrations integrations,
                             final String env,
                             final Map<AliasEnv, S3Client> amazonS3Map) {
        for (S3 s3 : integrations.getS3Integration().getS3()) {
            if (s3.isEnabled()) {
                S3Client resilientClient = connectionTemplate.executeWithRetry(
                        "S3 - " + s3.getAlias(),
                        () -> {
                            S3Client client = createAmazonS3(s3);
                            try {
                                client.listBuckets();
                                return client;
                            } catch (Exception e) {
                                client.close();
                                throw new DefaultFrameworkException(e.getMessage());
                            }
                        }
                );

                amazonS3Map.put(new AliasEnv(s3.getAlias(), env), resilientClient);
            }
        }
    }

    private S3Client createAmazonS3(final S3 s3) {
        AwsBasicCredentials basicAWSCredentials =
                AwsBasicCredentials.create(s3.getAccessKeyId(), s3.getSecretAccessKey());
        StaticCredentialsProvider awsStaticCredentialsProvider = StaticCredentialsProvider.create(basicAWSCredentials);
        return buildAmazonS3(s3, awsStaticCredentialsProvider);
    }

    private S3Client buildAmazonS3(final S3 s3,
                                   final StaticCredentialsProvider awsStaticCredentialsProvider) {
        return S3Client.builder()
                .region(Region.of(s3.getRegion()))
                .credentialsProvider(awsStaticCredentialsProvider)
                .endpointOverride(URI.create(s3.getEndpoint()))
                .forcePathStyle(true)
                .build();
    }
}
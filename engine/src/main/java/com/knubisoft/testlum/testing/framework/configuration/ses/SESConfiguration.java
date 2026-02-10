package com.knubisoft.testlum.testing.framework.configuration.ses;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnSESEnabledCondition;
import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.connection.ConnectionTemplate;
import com.knubisoft.testlum.testing.framework.configuration.connection.health.HealthCheckFactory;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Ses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.CONNECTION_INTEGRATION_DATA;

@Configuration
@Conditional({OnSESEnabledCondition.class})
@RequiredArgsConstructor
public class SESConfiguration {

    @Autowired(required = false)
    private final ConnectionTemplate connectionTemplate;

    @Bean
    public Map<AliasEnv, SesClient> sesClient() {
        Map<AliasEnv, SesClient> amazonSesMap = new HashMap<>();
        GlobalTestConfigurationProvider.get().getIntegrations()
                .forEach((env, integrations) -> addAmazonSes(integrations, env, amazonSesMap));
        return amazonSesMap;
    }

    private void addAmazonSes(final Integrations integrations,
                              final String env,
                              final Map<AliasEnv, SesClient> emailServiceMap) {
        for (Ses ses : integrations.getSesIntegration().getSes()) {
            if (ses.isEnabled()) {
                SesClient checkedSesClient = connectionTemplate.executeWithRetry(
                        String.format(CONNECTION_INTEGRATION_DATA, "SES", ses.getAlias()),
                        () -> createAmazonSes(ses),
                        HealthCheckFactory.forSes()
                );

                emailServiceMap.put(new AliasEnv(ses.getAlias(), env), checkedSesClient);
            }
        }
    }

    private SesClient createAmazonSes(final Ses ses) {
        AwsBasicCredentials basicAWSCredentials =
                AwsBasicCredentials.create(ses.getAccessKeyId(), ses.getSecretAccessKey());
        StaticCredentialsProvider awsStaticCredentialsProvider = StaticCredentialsProvider.create(basicAWSCredentials);
        return buildAmazonSes(ses, awsStaticCredentialsProvider);
    }

    private SesClient buildAmazonSes(final Ses ses,
                                     final StaticCredentialsProvider credentialsProvider) {
        return SesClient.builder()
                .region(Region.of(ses.getRegion()))
                .credentialsProvider(credentialsProvider)
                .endpointOverride(URI.create(ses.getEndpoint()))
                .build();
    }
}
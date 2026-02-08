package com.knubisoft.testlum.testing.framework.configuration.lambda;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnLambdaEnabledCondition;
import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.connection.ConnectionTemplate;
import com.knubisoft.testlum.testing.framework.configuration.connection.health.HealthCheckFactory;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Lambda;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.CONNECTION_INTEGRATION_DATA;

@Configuration
@Conditional({OnLambdaEnabledCondition.class})
@RequiredArgsConstructor
public class LambdaConfiguration {

    @Autowired(required = false)
    private final ConnectionTemplate connectionTemplate;

    @Bean
    public Map<AliasEnv, LambdaClient> awsLambdaClients() {
        final Map<AliasEnv, LambdaClient> lambdaClientMap = new HashMap<>();
        GlobalTestConfigurationProvider.get().getIntegrations()
                .forEach((env, integrations) -> addLambdaClient(integrations, env, lambdaClientMap));
        return lambdaClientMap;
    }

    private void addLambdaClient(final Integrations integrations,
                                 final String env,
                                 final Map<AliasEnv, LambdaClient> lambdaClientMap) {
        for (Lambda lambda : integrations.getLambdaIntegration().getLambda()) {
            if (lambda.isEnabled()) {
                LambdaClient lambdaClient = connectionTemplate.executeWithRetry(
                        String.format(CONNECTION_INTEGRATION_DATA, "Lambda", lambda.getAlias()),
                        () -> createLambdaClient(lambda),
                        HealthCheckFactory.forLambda()
                );
                lambdaClientMap.put(new AliasEnv(lambda.getAlias(), env), lambdaClient);
            }
        }
    }

    private LambdaClient createLambdaClient(final Lambda lambda) {
        AwsCredentials credentials = AwsBasicCredentials.create(lambda.getAccessKeyId(), lambda.getSecretAccessKey());
        return LambdaClient.builder()
                .endpointOverride(URI.create(lambda.getEndpoint()))
                .region(Region.of(lambda.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}

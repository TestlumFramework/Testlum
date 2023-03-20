package com.knubisoft.cott.testing.framework.configuration.lambda;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnLambdaEnabledCondition;
import com.knubisoft.cott.testing.framework.env.AliasEnv;
import com.knubisoft.cott.testing.model.global_config.Integrations;
import com.knubisoft.cott.testing.model.global_config.Lambda;
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

@Configuration
@Conditional({OnLambdaEnabledCondition.class})
public class LambdaConfiguration {

    @Bean
    public Map<AliasEnv, LambdaClient> awsLambdaClients() {
        final Map<AliasEnv, LambdaClient> lambdaClientMap = new HashMap<>();
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach((env, integrations) -> addLambdaClient(integrations, env, lambdaClientMap));
        return lambdaClientMap;
    }

    private void addLambdaClient(final Integrations integrations,
                                 final String env,
                                 final Map<AliasEnv, LambdaClient> lambdaClientMap) {
        for (Lambda lambda : integrations.getLambdaIntegration().getLambda()) {
            if (lambda.isEnabled()) {
                lambdaClientMap.put(new AliasEnv(lambda.getAlias(), env), createLambdaClient(lambda));
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

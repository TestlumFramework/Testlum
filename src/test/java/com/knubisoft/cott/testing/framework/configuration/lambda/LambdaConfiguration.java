package com.knubisoft.cott.testing.framework.configuration.lambda;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnLambdaEnabledCondition;
import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
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
import java.util.List;
import java.util.Map;

@Configuration
@Conditional({OnLambdaEnabledCondition.class})
public class LambdaConfiguration {

    @Bean
    public Map<String, LambdaClient> awsLambdaClients() {
        final Map<String, LambdaClient> lambdaClientMap = new HashMap<>();
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach(((s, integrations) -> addLambdaClient(s, integrations.getLambdaIntegration().getLambda(),
                        lambdaClientMap)));
        return lambdaClientMap;
    }

    private void addLambdaClient(final String envName,
                                 final List<Lambda> lambdas,
                                 final Map<String, LambdaClient> lambdaClientMap) {
        for (Lambda lambda : lambdas) {
            if (lambda.isEnabled()) {
                lambdaClientMap.put(envName + DelimiterConstant.UNDERSCORE + lambda.getAlias(),
                        createLambdaClient(lambda));
            }
        }
    }

    public LambdaClient createLambdaClient(final Lambda lambda) {
        AwsCredentials credentials = AwsBasicCredentials.create(lambda.getAccessKeyId(), lambda.getSecretAccessKey());
        return LambdaClient.builder()
                .endpointOverride(URI.create(lambda.getEndpoint()))
                .region(Region.of(lambda.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}

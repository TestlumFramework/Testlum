package com.testlum.testing.framework.configuration.email;

import com.testlum.testing.connection.ConnectionTemplate;
import com.testlum.testing.connection.IntegrationHealthCheck;
import com.testlum.testing.framework.EnvToIntegrationMap;
import com.testlum.testing.framework.condition.OnEmailEnabledCondition;
import com.testlum.testing.framework.constant.LogMessage;
import com.testlum.testing.framework.env.AliasEnv;
import com.testlum.testing.framework.service.EmailInboxService;
import com.testlum.testing.model.global_config.Email;
import com.testlum.testing.model.global_config.Integrations;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring configuration for registering resilient email inbox service instances.
 */
@Configuration
@Conditional(OnEmailEnabledCondition.class)
@RequiredArgsConstructor
public class EmailConfiguration {

    private final ConnectionTemplate connectionTemplate;

    /**
     * Creates and registers a map of alias/environment pairs to resilient EmailInboxService instances.
     *
     * @param envToIntegrations map of environment names to parsed integrations
     * @return map of alias and environment pairs to configured EmailInboxService beans
     */
    @Bean("emailInboxServices")
    public Map<AliasEnv, EmailInboxService> emailInboxServices(final EnvToIntegrationMap envToIntegrations) {
        final Map<AliasEnv, EmailInboxService> serviceMap = new HashMap<>();
        envToIntegrations.forEach((env, integrations) -> this.addServicesToMap(integrations, env, serviceMap));
        return serviceMap;
    }

    private void addServicesToMap(final Integrations integrations,
                                  final String env,
                                  final Map<AliasEnv, EmailInboxService> serviceMap) {
        if (integrations.getEmailIntegration() == null) {
            return;
        }
        for (final Email email : integrations.getEmailIntegration().getEmail()) {
            if (email.isEnabled()) {
                final EmailInboxService resilientService = this.connectionTemplate.executeWithRetry(
                        String.format(LogMessage.CONNECTION_INTEGRATION_DATA, "EMAIL", email.getAlias()),
                        () -> new EmailInboxService(email),
                        forEmail()
                );
                serviceMap.put(new AliasEnv(email.getAlias(), env), resilientService);
            }
        }
    }

    private IntegrationHealthCheck<EmailInboxService> forEmail() {
        return EmailInboxService::testConnection;
    }
}

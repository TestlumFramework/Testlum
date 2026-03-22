package com.knubisoft.testlum.testing.framework.configuration.twilio;

import com.knubisoft.testlum.testing.connection.ConnectionTemplate;
import com.knubisoft.testlum.testing.connection.IntegrationHealthCheck;
import com.knubisoft.testlum.testing.framework.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.condition.OnTwilioEnabledCondition;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Twilio;
import com.twilio.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import com.knubisoft.testlum.testing.framework.constant.LogMessage;

@Configuration
@Conditional(OnTwilioEnabledCondition.class)
@RequiredArgsConstructor
public class TwilioConfiguration {

    private final ConnectionTemplate connectionTemplate;

    @Bean
    public Map<AliasEnv, Twilio> twilio(
            final GlobalTestConfigurationProvider.EnvToIntegrationMap envToIntegrations) {
        Map<AliasEnv, Twilio> twilioMap = new HashMap<>();
        envToIntegrations
                .forEach((env, integrations) -> addTwilioToMap(integrations, env, twilioMap));
        return twilioMap;
    }

    private void addTwilioToMap(final Integrations integrations,
                                final String env,
                                final Map<AliasEnv, Twilio> twilioMap) {
        for (Twilio twilio : integrations.getTwilioIntegration().getTwilio()) {
            if (twilio.isEnabled()) {
                Twilio checkedTwilioConfig = connectionTemplate.executeWithRetry(
                        String.format(LogMessage.CONNECTION_INTEGRATION_DATA, "Twilio", twilio.getAlias()),
                        () -> twilio,
                        forTwilio()
                );

                twilioMap.put(new AliasEnv(twilio.getAlias(), env), checkedTwilioConfig);
            }
        }
    }

    private IntegrationHealthCheck<Twilio> forTwilio() {
        return twilio -> {
            com.twilio.Twilio.init(twilio.getAccountSid(), twilio.getAuthToken());
            try {
                com.twilio.rest.api.v2010.Account.fetcher(twilio.getAccountSid()).fetch();
            } catch (com.twilio.exception.AuthenticationException authEx) {
                throw new DefaultFrameworkException("Twilio Auth Failed - " + authEx.getMessage());
            } catch (ApiException e) {
                throw new DefaultFrameworkException(
                        "Twilio API unreachable - " + e.getMessage() + " " + e.getMoreInfo());
            } catch (Exception e) {
                throw new DefaultFrameworkException("Twilio API unreachable - " + e.getMessage());
            }
        };
    }
}

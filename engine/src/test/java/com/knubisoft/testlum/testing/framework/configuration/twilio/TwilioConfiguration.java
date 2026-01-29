package com.knubisoft.testlum.testing.framework.configuration.twilio;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnTwilioEnabledCondition;
import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.connection.ConnectionTemplate;
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

@Configuration
@Conditional(OnTwilioEnabledCondition.class)
@RequiredArgsConstructor
public class TwilioConfiguration {

    private final ConnectionTemplate connectionTemplate;

    @Bean
    public Map<AliasEnv, Twilio> twilio() {
        Map<AliasEnv, Twilio> twilioMap = new HashMap<>();
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach((env, integrations) -> addTwilioToMap(integrations, env, twilioMap));
        return twilioMap;
    }

    private void addTwilioToMap(final Integrations integrations,
                                final String env,
                                final Map<AliasEnv, Twilio> twilioMap) {
        for (Twilio twilio : integrations.getTwilioIntegration().getTwilio()) {
            if (twilio.isEnabled()) {
                Twilio checkedTwilioConfig = connectionTemplate.executeWithRetry(
                        "Twilio - " + twilio.getAlias(),
                        () -> {
                            com.twilio.Twilio.init(twilio.getAccountSid(), twilio.getAuthToken());
                            try {
                                com.twilio.rest.api.v2010.Account.fetcher(twilio.getAccountSid()).fetch();
                                return twilio;
                            } catch (com.twilio.exception.AuthenticationException authEx) {
                                throw new DefaultFrameworkException("Twilio Auth Failed - " + authEx.getMessage());
                            } catch (ApiException e) {
                                throw new DefaultFrameworkException("Twilio API unreachable - " + e.getMessage() + " " + e.getMoreInfo());
                            } catch (Exception e) {
                                throw new DefaultFrameworkException("Twilio API unreachable - " + e.getMessage());
                            }
                        }
                );

                twilioMap.put(new AliasEnv(twilio.getAlias(), env), checkedTwilioConfig);
            }
        }
    }
}

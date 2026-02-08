package com.knubisoft.testlum.testing.framework.configuration.twilio;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnTwilioEnabledCondition;
import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.connection.ConnectionTemplate;
import com.knubisoft.testlum.testing.framework.configuration.connection.health.HealthCheckFactory;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Twilio;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.CONNECTION_INTEGRATION_DATA;

@Configuration
@Conditional(OnTwilioEnabledCondition.class)
@RequiredArgsConstructor
public class TwilioConfiguration {

    @Autowired(required = false)
    private final ConnectionTemplate connectionTemplate;

    @Bean
    public Map<AliasEnv, Twilio> twilio() {
        Map<AliasEnv, Twilio> twilioMap = new HashMap<>();
        GlobalTestConfigurationProvider.get().getIntegrations()
                .forEach((env, integrations) -> addTwilioToMap(integrations, env, twilioMap));
        return twilioMap;
    }

    private void addTwilioToMap(final Integrations integrations,
                                final String env,
                                final Map<AliasEnv, Twilio> twilioMap) {
        for (Twilio twilio : integrations.getTwilioIntegration().getTwilio()) {
            if (twilio.isEnabled()) {
                Twilio checkedTwilioConfig = connectionTemplate.executeWithRetry(
                        String.format(CONNECTION_INTEGRATION_DATA, "Twilio", twilio.getAlias()),
                        () -> twilio,
                        HealthCheckFactory.forTwilio()
                );

                twilioMap.put(new AliasEnv(twilio.getAlias(), env), checkedTwilioConfig);
            }
        }
    }
}

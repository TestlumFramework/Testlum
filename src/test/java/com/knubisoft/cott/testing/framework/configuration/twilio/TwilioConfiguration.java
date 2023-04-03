package com.knubisoft.cott.testing.framework.configuration.twilio;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnTwilioEnabledCondition;
import com.knubisoft.cott.testing.framework.env.AliasEnv;
import com.knubisoft.cott.testing.model.global_config.Integrations;
import com.knubisoft.cott.testing.model.global_config.Twilio;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Conditional(OnTwilioEnabledCondition.class)
public class TwilioConfiguration {

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
                twilioMap.put(new AliasEnv(twilio.getAlias(), env), twilio);
            }
        }
    }
}

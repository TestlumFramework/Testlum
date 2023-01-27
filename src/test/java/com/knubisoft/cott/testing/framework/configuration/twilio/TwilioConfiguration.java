package com.knubisoft.cott.testing.framework.configuration.twilio;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnTwilioEnabledCondition;
import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.model.global_config.Twilio;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.UNDERSCORE;

@Configuration
@Conditional(OnTwilioEnabledCondition.class)
public class TwilioConfiguration {

    @Bean
    public Map<String, Twilio> twilio() {
        Map<String, Twilio> twilioMap = new HashMap<>();
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach(((s, integrations) -> addToMap(s, integrations.getTwilioIntegration().getTwilio(),
                        twilioMap)));
        return twilioMap;
    }

    private void addToMap(final String envName, final List<Twilio> twilios, final Map<String, Twilio> twilioMap) {
        twilios.stream()
                .filter(Twilio::isEnabled)
                .forEach(twilio -> twilioMap.put(envName + UNDERSCORE + twilio.getAlias(),
                        (Twilio) Function.identity()));
    }
}

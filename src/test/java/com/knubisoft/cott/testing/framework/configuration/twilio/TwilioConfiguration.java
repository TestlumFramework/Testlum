package com.knubisoft.cott.testing.framework.configuration.twilio;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnTwilioEnabledCondition;
import com.knubisoft.cott.testing.model.global_config.Twilio;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@Conditional(OnTwilioEnabledCondition.class)
public class TwilioConfiguration {

    @Bean
    public Map<String, Twilio> twilio() {
        return GlobalTestConfigurationProvider
                .getIntegrations()
                .getTwilioIntegration()
                .getTwilio()
                .stream()
                .filter(Twilio::isEnabled)
                .collect(Collectors.toMap(Twilio::getAlias, Function.identity()));
    }
}

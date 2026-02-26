package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Twilio;
import com.knubisoft.testlum.testing.model.global_config.TwilioIntegration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OnTwilioEnabledCondition extends AbstractCondition<Twilio> {

    @Override
    List<? extends Integration> getIntegrations(final Integrations integrations) {
        return Optional.ofNullable(integrations.getTwilioIntegration())
                .map(TwilioIntegration::getTwilio)
                .orElse(null);
    }
}

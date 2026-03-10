package com.knubisoft.testlum.testing.framework.condition;

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
    protected Optional<List<? extends Integration>> getIntegrations(final Optional<Integrations> integrations) {
        return integrations.map(Integrations::getTwilioIntegration).map(TwilioIntegration::getTwilio);
    }
}

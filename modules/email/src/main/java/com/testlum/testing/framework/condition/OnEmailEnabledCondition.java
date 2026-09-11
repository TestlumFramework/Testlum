package com.testlum.testing.framework.condition;

import com.testlum.testing.model.global_config.Email;
import com.testlum.testing.model.global_config.EmailIntegration;
import com.testlum.testing.model.global_config.Integration;
import com.testlum.testing.model.global_config.Integrations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Condition that checks if email inbox integration is configured and enabled in the current environment.
 */
@Component
public class OnEmailEnabledCondition extends AbstractCondition<Email> {

    @Override
    protected Optional<List<? extends Integration>> getIntegrations(final Optional<Integrations> integrations) {
        return integrations.map(Integrations::getEmailIntegration).map(EmailIntegration::getEmail);
    }
}

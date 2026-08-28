package com.testlum.testing.framework.condition;

import com.testlum.testing.model.global_config.Integration;
import com.testlum.testing.model.global_config.Integrations;
import com.testlum.testing.model.global_config.Smtp;
import com.testlum.testing.model.global_config.SmtpIntegration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OnSmtpEnabledCondition extends AbstractCondition<Smtp> {

    @Override
    protected Optional<List<? extends Integration>> getIntegrations(final Optional<Integrations> integrations) {
        return integrations.map(Integrations::getSmtpIntegration).map(SmtpIntegration::getSmtp);
    }
}

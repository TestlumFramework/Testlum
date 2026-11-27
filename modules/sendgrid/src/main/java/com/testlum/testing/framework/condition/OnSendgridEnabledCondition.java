package com.testlum.testing.framework.condition;

import com.testlum.testing.model.global_config.Integration;
import com.testlum.testing.model.global_config.Integrations;
import com.testlum.testing.model.global_config.Sendgrid;
import com.testlum.testing.model.global_config.SendgridIntegration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OnSendgridEnabledCondition extends AbstractCondition<Sendgrid> {

    @Override
    protected Optional<List<? extends Integration>> getIntegrations(final Optional<Integrations> integrations) {
        return integrations.map(Integrations::getSendgridIntegration).map(SendgridIntegration::getSendgrid);
    }
}

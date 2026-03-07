package com.knubisoft.testlum.testing.framework.condition;

import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Smtp;
import com.knubisoft.testlum.testing.model.global_config.SmtpIntegration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OnSmtpEnabledCondition extends AbstractCondition<Smtp> {

    @Override
    List<? extends Integration> getIntegrations(final Integrations integrations) {
        return Optional.ofNullable(integrations.getSmtpIntegration())
                .map(SmtpIntegration::getSmtp)
                .orElse(null);
    }
}

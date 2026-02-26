package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Sqs;
import com.knubisoft.testlum.testing.model.global_config.SqsIntegration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OnSQSEnabledCondition extends AbstractCondition<Sqs> {

    @Override
    List<? extends Integration> getIntegrations(final Integrations integrations) {
        return Optional.ofNullable(integrations.getSqsIntegration())
                .map(SqsIntegration::getSqs)
                .orElse(null);
    }
}

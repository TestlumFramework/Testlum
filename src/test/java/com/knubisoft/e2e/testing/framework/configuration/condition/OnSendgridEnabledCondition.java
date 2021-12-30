package com.knubisoft.e2e.testing.framework.configuration.condition;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.model.global_config.Sendgrid;
import com.knubisoft.e2e.testing.model.global_config.Sendgrids;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnSendgridEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext conditionContext,
                           final AnnotatedTypeMetadata annotatedTypeMetadata) {
        final Sendgrids sendgrids = GlobalTestConfigurationProvider.provide().getSendgrids();
        if (Objects.nonNull(sendgrids)) {
            return sendgrids.getSendgrid()
                    .stream().anyMatch(Sendgrid::isEnabled);
        }
        return false;
    }
}

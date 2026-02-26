package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Lambda;
import com.knubisoft.testlum.testing.model.global_config.LambdaIntegration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OnLambdaEnabledCondition extends AbstractCondition<Lambda> {

    @Override
    List<? extends Integration> getIntegrations(final Integrations integrations) {
        return Optional.ofNullable(integrations.getLambdaIntegration())
                .map(LambdaIntegration::getLambda)
                .orElse(null);
    }
}

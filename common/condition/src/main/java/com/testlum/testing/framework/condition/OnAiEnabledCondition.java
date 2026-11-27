package com.testlum.testing.framework.condition;

import com.testlum.testing.model.global_config.Ai;
import com.testlum.testing.model.global_config.AiIntegration;
import com.testlum.testing.model.global_config.Integration;
import com.testlum.testing.model.global_config.Integrations;

import java.util.List;
import java.util.Optional;

public class OnAiEnabledCondition extends AbstractCondition<Ai> {

    @Override
    protected Optional<List<? extends Integration>> getIntegrations(final Optional<Integrations> integrations) {
        return integrations.map(Integrations::getAiIntegration).map(AiIntegration::getAi);
    }
}

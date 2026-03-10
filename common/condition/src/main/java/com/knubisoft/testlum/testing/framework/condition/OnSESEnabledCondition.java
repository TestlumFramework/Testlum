package com.knubisoft.testlum.testing.framework.condition;

import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Ses;
import com.knubisoft.testlum.testing.model.global_config.SesIntegration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OnSESEnabledCondition extends AbstractCondition<Ses> {

    @Override
    protected Optional<List<? extends Integration>> getIntegrations(final Optional<Integrations> integrations) {
        return integrations.map(Integrations::getSesIntegration).map(SesIntegration::getSes);
    }
}

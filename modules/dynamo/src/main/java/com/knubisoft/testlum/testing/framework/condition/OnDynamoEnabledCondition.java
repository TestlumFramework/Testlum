package com.knubisoft.testlum.testing.framework.condition;

import com.knubisoft.testlum.testing.model.global_config.Dynamo;
import com.knubisoft.testlum.testing.model.global_config.DynamoIntegration;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OnDynamoEnabledCondition extends AbstractCondition<Dynamo> {

    @Override
    protected Optional<List<? extends Integration>> getIntegrations(final Optional<Integrations> integrations) {
        return integrations.map(Integrations::getDynamoIntegration).map(DynamoIntegration::getDynamo);
    }
}

package com.knubisoft.testlum.testing.framework.configuration.condition;

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
    List<? extends Integration> getIntegrations(final Integrations integrations) {
        return Optional.ofNullable(integrations.getDynamoIntegration())
                .map(DynamoIntegration::getDynamo)
                .orElse(null);
    }
}

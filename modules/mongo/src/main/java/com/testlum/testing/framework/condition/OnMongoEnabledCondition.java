package com.testlum.testing.framework.condition;

import com.testlum.testing.model.global_config.Integration;
import com.testlum.testing.model.global_config.Integrations;
import com.testlum.testing.model.global_config.Mongo;
import com.testlum.testing.model.global_config.MongoIntegration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OnMongoEnabledCondition extends AbstractCondition<Mongo> {

    @Override
    protected Optional<List<? extends Integration>> getIntegrations(final Optional<Integrations> integrations) {
        return integrations.map(Integrations::getMongoIntegration).map(MongoIntegration::getMongo);
    }
}

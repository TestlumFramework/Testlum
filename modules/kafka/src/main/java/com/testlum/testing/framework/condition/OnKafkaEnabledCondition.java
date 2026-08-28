package com.testlum.testing.framework.condition;

import com.testlum.testing.model.global_config.Integration;
import com.testlum.testing.model.global_config.Integrations;
import com.testlum.testing.model.global_config.Kafka;
import com.testlum.testing.model.global_config.KafkaIntegration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OnKafkaEnabledCondition extends AbstractCondition<Kafka> {

    @Override
    protected Optional<List<? extends Integration>> getIntegrations(final Optional<Integrations> integrations) {
        return integrations.map(Integrations::getKafkaIntegration).map(KafkaIntegration::getKafka);
    }
}

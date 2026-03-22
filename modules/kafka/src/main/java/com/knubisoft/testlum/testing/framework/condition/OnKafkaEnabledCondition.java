package com.knubisoft.testlum.testing.framework.condition;

import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Kafka;
import com.knubisoft.testlum.testing.model.global_config.KafkaIntegration;
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

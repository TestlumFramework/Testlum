package com.testlum.testing.framework.condition;

import com.testlum.testing.model.global_config.Clickhouse;
import com.testlum.testing.model.global_config.ClickhouseIntegration;
import com.testlum.testing.model.global_config.Integration;
import com.testlum.testing.model.global_config.Integrations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OnClickhouseEnabledCondition extends AbstractCondition<Clickhouse> {

    @Override
    protected Optional<List<? extends Integration>> getIntegrations(final Optional<Integrations> integrations) {
        return integrations.map(Integrations::getClickhouseIntegration).map(ClickhouseIntegration::getClickhouse);
    }
}

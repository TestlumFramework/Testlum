package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.model.global_config.Clickhouse;
import com.knubisoft.cott.testing.model.global_config.ClickhouseIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnClickhouseEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext conditionContext,
                           final AnnotatedTypeMetadata annotatedTypeMetadata) {
        final ClickhouseIntegration clickhouseIntegration =
                GlobalTestConfigurationProvider.getDefaultIntegration().getClickhouseIntegration();
        if (Objects.nonNull(clickhouseIntegration)) {
            return clickhouseIntegration.getClickhouse().stream().anyMatch(Clickhouse::isEnabled);
        }
        return false;
    }
}

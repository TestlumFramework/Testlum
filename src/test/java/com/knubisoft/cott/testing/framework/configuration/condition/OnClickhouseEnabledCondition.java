package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.util.ConfigUtil;
import com.knubisoft.cott.testing.model.global_config.ClickhouseIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnClickhouseEnabledCondition implements Condition {

    private final ClickhouseIntegration clickhouseIntegration =
            GlobalTestConfigurationProvider.getDefaultIntegration().getClickhouseIntegration();

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
//        GlobalTestConfigurationProvider.getIntegrations()
//                .values().stream()
//                .filter(v -> Objects.nonNull(v.getClickhouseIntegration()))
//                .findFirst()
//                .map(Integrations::getClickhouseIntegration)
//                .filter(c -> c.getClickhouse().stream().anyMatch(Clickhouse::isEnabled))
//                .isPresent();

        if (Objects.nonNull(clickhouseIntegration)) {
            return ConfigUtil.isIntegrationEnabled(clickhouseIntegration.getClickhouse());
        }
        return false;
    }
}

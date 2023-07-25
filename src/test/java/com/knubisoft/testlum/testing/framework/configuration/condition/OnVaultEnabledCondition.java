package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.util.IntegrationsUtil;
import com.knubisoft.testlum.testing.model.global_config.VaultIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnVaultEnabledCondition implements Condition {

    private final VaultIntegration vaultIntegration =
            GlobalTestConfigurationProvider.getDefaultIntegrations().getVaultIntegration();

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        if (Objects.nonNull(vaultIntegration)) {
            return IntegrationsUtil.isEnabled(vaultIntegration.getVault());
        }
        return false;
    }
}

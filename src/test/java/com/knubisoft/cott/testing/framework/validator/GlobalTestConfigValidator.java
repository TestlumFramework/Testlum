package com.knubisoft.cott.testing.framework.validator;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.model.global_config.GlobalTestConfiguration;

import java.io.File;
import java.util.Objects;

public class GlobalTestConfigValidator implements XMLValidator<GlobalTestConfiguration> {
    private final SubscriptionValidator subscriptionValidator = new StripeValidationService();

    @Override
    public void validate(final GlobalTestConfiguration globalTestConfig, final File xmlFile) {
        checkIsActiveSubscription(globalTestConfig);
    }

    private void checkIsActiveSubscription(final GlobalTestConfiguration globalTestConfig) {
        if (Objects.isNull(globalTestConfig.getSubscription())) {
            throw new DefaultFrameworkException("Cannot find customer subscription configuration");
        }
        if ("free".equalsIgnoreCase(globalTestConfig.getSubscription().getType().value())) {
            return;
        }
        try {
            subscriptionValidator.checkSubscription(globalTestConfig);
        } catch (Exception e) {
            LogUtil.logException(e);
            throw e;
        }
    }
}

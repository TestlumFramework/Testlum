package com.knubisoft.cott.testing.framework.validator;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.cott.testing.model.global_config.Integration;
import com.knubisoft.cott.testing.model.global_config.Integrations;
import org.junit.platform.commons.util.ReflectionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.SAME_INTEGRATION_ALIASES;

public class GlobalTestConfigValidator implements XMLValidator<GlobalTestConfiguration> {
    private final SubscriptionValidator subscriptionValidator = new StripeValidationService();
    private final List<List<? extends Integration>> integrationLists = new ArrayList<>();

    @Override
    public void validate(final GlobalTestConfiguration globalTestConfig, final File xmlFile) {
        checkIsActiveSubscription(globalTestConfig);
        checkIntegrations(globalTestConfig);
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

    private void checkIntegrations(GlobalTestConfiguration globalTestConfig) {
        if (Objects.nonNull(globalTestConfig.getIntegrations())) {
            getIntegrations(globalTestConfig.getIntegrations());
            checkIntegrationsAliases();
        }
    }

    private void getIntegrations(final Integrations integrations) {
        Arrays.stream(integrations.getClass().getDeclaredMethods())
                .filter(method -> Objects.nonNull(method) && method.getName().startsWith("get"))
                .map(method -> ReflectionUtils.invokeMethod(method, integrations))
                .filter(Objects::nonNull)
                .forEach(integrationObject -> Arrays.stream(integrationObject.getClass().getDeclaredMethods())
                        .filter(method -> Objects.nonNull(method) && method.getReturnType().equals(List.class))
                        .map(method -> ReflectionUtils.invokeMethod(method, integrationObject))
                        .forEach(integrationList ->
                                integrationLists.add((List<? extends Integration>) integrationList)));
    }

    private void checkIntegrationsAliases() {
        integrationLists.forEach(integrationList -> integrationList.forEach(integration -> {
                    if (integrationList.stream().map(Integration::getAlias)
                            .filter(alias -> alias.equalsIgnoreCase(integration.getAlias())).count() > 1) {
                        throw new DefaultFrameworkException(SAME_INTEGRATION_ALIASES,
                                integration.getClass().getSimpleName(), integration.getAlias());
                    }
                }
        ));
    }

}

package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import lombok.experimental.UtilityClass;

import java.util.List;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.ALIAS_NOT_FOUND;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.API_NOT_FOUND;

@UtilityClass
public class IntegrationsUtil {

    public <T extends Integration> T findApiForAlias(final List<T> apiIntegrations, final String alias) {
        return filterIntegrationByAlias(apiIntegrations, alias, API_NOT_FOUND);
    }

    public <T extends Integration> T findForAlias(final List<T> integrationList, final String alias) {
        return filterIntegrationByAlias(integrationList, alias, ALIAS_NOT_FOUND);
    }

    private <T extends Integration> T filterIntegrationByAlias(final List<T> integrations,
                                                               final String alias,
                                                               final String message) {
        return integrations.stream()
                .filter(Integration::isEnabled)
                .filter(integration -> integration.getAlias().equals(alias))
                .findFirst()
                .orElseThrow(() -> new DefaultFrameworkException(message, alias));
    }

    public <T extends Integration> boolean isEnabled(final List<T> integrations) {
        return integrations.stream().anyMatch(Integration::isEnabled);
    }
}

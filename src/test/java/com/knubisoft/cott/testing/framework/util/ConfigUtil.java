package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.model.global_config.Integration;
import lombok.experimental.UtilityClass;

import java.util.List;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.ALIAS_NOT_FOUND;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.API_NOT_FOUND;

@UtilityClass
public class ConfigUtil {

    public <T extends Integration> T findApiForAlias(final List<T> apiIntegrations, final String alias) {
        return findIntegrationByAlias(apiIntegrations, alias, API_NOT_FOUND);
    }

    public <T extends Integration> T findIntegrationForAlias(final List<T> integrationList, final String alias) {
        return findIntegrationByAlias(integrationList, alias, ALIAS_NOT_FOUND);
    }

    private <T extends Integration> T findIntegrationByAlias(final List<T> integrations,
                                                             final String alias,
                                                             final String message) {
        return integrations.stream()
                .filter(Integration::isEnabled)
                .filter(api -> api.getAlias().equalsIgnoreCase(alias))
                .findFirst()
                .orElseThrow(() -> new DefaultFrameworkException(message, alias));
    }

    public <T extends Integration> boolean isIntegrationEnabled(final List<T> integrations) {
        return integrations.stream().anyMatch(Integration::isEnabled);
    }
}

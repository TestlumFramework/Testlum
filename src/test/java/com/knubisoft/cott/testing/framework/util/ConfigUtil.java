package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.model.global_config.Integration;
import lombok.experimental.UtilityClass;

import java.util.List;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.ALIAS_NOT_FOUND;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.API_NOT_FOUND;

@UtilityClass
public class ConfigUtil {

    public Integration findApiForAlias(final List<? extends Integration> apiIntegrations, final String alias) {
        return findIntegrationByAlias(apiIntegrations, alias, API_NOT_FOUND);
    }

    public void checkIntegrationForAlias(final List<? extends Integration> integrationList, final String alias) {
        findIntegrationByAlias(integrationList, alias, ALIAS_NOT_FOUND);
    }

    private Integration findIntegrationByAlias(final List<? extends Integration> integrations,
                                               final String alias,
                                               final String message) {
        return integrations.stream()
                .filter(Integration::isEnabled)
                .filter(api -> api.getAlias().equalsIgnoreCase(alias))
                .findFirst()
                .orElseThrow(() -> new DefaultFrameworkException(message, alias));
    }
}

package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class IntegrationsProviderImpl implements IntegrationsProvider {

    private final IntegrationsUtil integrationsUtil;

    @Override
    public <T extends Integration> T findForAliasEnv(final Class<T> clazz, final AliasEnv aliasEnv) {
        return integrationsUtil.findForAliasEnv(clazz, aliasEnv);
    }

    @Override
    public <T extends Integration> List<T> findListByEnv(final Class<T> clazz, final String env) {
        return integrationsUtil.findListByEnv(clazz, env);
    }

    @Override
    public <T extends Integration> T findApiForAlias(final List<T> apiIntegrations, final String env) {
        return integrationsUtil.findApiForAlias(apiIntegrations, env);
    }

    @Override
    public <T extends Integration> boolean isEnabled(final List<T> integrations) {
        return false;
    }

}

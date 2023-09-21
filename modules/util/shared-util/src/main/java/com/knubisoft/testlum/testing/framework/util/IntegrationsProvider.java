package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Integration;

import java.util.List;

public interface IntegrationsProvider {

    <T extends Integration> T findForAliasEnv(Class<T> clazz, AliasEnv aliasEnv);
    <T extends Integration> List<T> findListByEnv(Class<T> clazz, String env);
    <T extends Integration> T findApiForAlias(List<T> apiIntegrations, String alias);
    <T extends Integration> T findForAlias(List<T> integrationList, String alias);
    <T extends Integration> boolean isEnabled(List<T> integrations);
}

package com.testlum.testing.framework.context;

import com.testlum.testing.framework.constant.DelimiterConstant;
import com.testlum.testing.framework.db.AbstractStorageOperation;
import com.testlum.testing.model.global_config.Integration;
import com.testlum.testing.model.global_config.Integrations;

import java.util.List;
import java.util.Map;

public abstract class AbstractAliasAdapter implements AliasAdapter {

    private final AbstractStorageOperation operation;
    private final Integrations integrations;

    protected AbstractAliasAdapter(final AbstractStorageOperation operation,
                                   final Integrations integrations) {
        this.operation = operation;
        this.integrations = integrations;
    }

    @Override
    public void apply(final Map<String, AbstractStorageOperation> aliasMap) {
        for (Integration integration : getIntegrationList(integrations)) {
            if (integration.isEnabled()) {
                aliasMap.put(getStorageName() + DelimiterConstant.UNDERSCORE + integration.getAlias(), operation);
            }
        }
    }

    protected abstract List<? extends Integration> getIntegrationList(Integrations integrations);

    protected abstract String getStorageName();
}

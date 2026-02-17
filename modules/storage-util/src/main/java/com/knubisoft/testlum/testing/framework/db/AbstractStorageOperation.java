package com.knubisoft.testlum.testing.framework.db;

import com.knubisoft.testlum.testing.framework.db.source.Source;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.util.IntegrationsProvider;
import com.knubisoft.testlum.testing.model.global_config.StorageIntegration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public abstract class AbstractStorageOperation {

    @Autowired
    private IntegrationsProvider integrationsProvider;

    public List<StorageOperationResult> apply(final List<Source> sources, final String databaseAlias) {
        return sources.stream()
                .map(source -> apply(source, databaseAlias))
                .toList();
    }

    public abstract StorageOperationResult apply(Source source, String databaseAlias);

    public <T extends StorageIntegration> boolean isTruncate(final Class<T> clazz, final AliasEnv aliasEnv) {
        T integration = integrationsProvider.findForAliasEnv(clazz, aliasEnv);
        return integration.isTruncate();
    }

    public abstract void clearSystem();

    @Getter
    @RequiredArgsConstructor
    public static class StorageOperationResult {
        private final Object raw;
    }

    @Getter
    @Setter
    @RequiredArgsConstructor
    @AllArgsConstructor
    public static class QueryResult<T> {
        private final String query;
        private T content;
    }
}

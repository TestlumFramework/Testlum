package com.knubisoft.testlum.testing.framework.db;

import com.knubisoft.testlum.testing.framework.db.source.Source;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.util.IntegrationsUtil;
import com.knubisoft.testlum.testing.model.global_config.StorageIntegration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

public interface StorageOperation {

    default List<StorageOperationResult> apply(List<Source> sources, String databaseAlias) {
        return sources.stream()
                .map(source -> apply(source, databaseAlias))
                .collect(Collectors.toList());
    }

    StorageOperationResult apply(Source source, String databaseAlias);

    default <T extends StorageIntegration> boolean isTruncate(Class<T> clazz, AliasEnv aliasEnv) {
        T integration = IntegrationsUtil.findForAliasEnv(clazz, aliasEnv);
        return integration.isTruncate();
    }

    void clearSystem();

    @Getter
    @RequiredArgsConstructor
    class StorageOperationResult {
        private final Object raw;
    }

    @Getter
    @Setter
    @RequiredArgsConstructor
    @AllArgsConstructor
    class QueryResult<T> {
        private final String query;
        private T content;
    }
}

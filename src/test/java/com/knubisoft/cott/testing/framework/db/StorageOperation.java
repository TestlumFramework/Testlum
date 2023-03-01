package com.knubisoft.cott.testing.framework.db;

import com.knubisoft.cott.testing.framework.db.source.Source;
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

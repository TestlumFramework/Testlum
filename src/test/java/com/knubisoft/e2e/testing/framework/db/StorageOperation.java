package com.knubisoft.e2e.testing.framework.db;

import com.knubisoft.e2e.testing.framework.db.source.Source;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

public interface StorageOperation {

    default List<StorageOperationResult> apply(List<Source> sources,
                                               String databaseName) {
        return sources.stream().map(source -> apply(source, databaseName)).collect(Collectors.toList());
    }

    StorageOperationResult apply(Source source, String databaseName);

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

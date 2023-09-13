package com.knubisoft.testlum.testing.framework.context;

import com.knubisoft.testlum.testing.framework.db.StorageOperation;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.ALIAS_BY_STORAGE_NAME_NOT_FOUND;

@RequiredArgsConstructor
public class AliasToStorageOperationImpl implements AliasToStorageOperation {

    private final Map<String, StorageOperation> alias;

    public StorageOperation getByNameOrThrow(final String name) {
        String adapterName = name.toUpperCase(Locale.US);
        StorageOperation storageOperation = this.alias.get(adapterName);
        if (Objects.isNull(storageOperation)) {
            throw new DefaultFrameworkException(ALIAS_BY_STORAGE_NAME_NOT_FOUND, adapterName, alias.keySet());
        }
        return storageOperation;
    }

    public Map<String, StorageOperation> getAlias() {
        return Collections.unmodifiableMap(alias);
    }

}

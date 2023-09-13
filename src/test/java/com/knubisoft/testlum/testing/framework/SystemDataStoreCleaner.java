package com.knubisoft.testlum.testing.framework;

import com.knubisoft.testlum.testing.framework.context.AliasToStorageOperation;
import com.knubisoft.testlum.testing.framework.db.StorageOperation;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SystemDataStoreCleaner {

    public void clearAll(final AliasToStorageOperation aliasToStorageOperation) {
        Map<String, StorageOperation> metadataMap = aliasToStorageOperation.getAlias();
        for (final StorageOperation storageOperation : metadataMap.values()) {
            storageOperation.clearSystem();
        }
    }
}

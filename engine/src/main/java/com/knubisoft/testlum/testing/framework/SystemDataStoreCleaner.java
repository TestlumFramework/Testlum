package com.knubisoft.testlum.testing.framework;

import com.knubisoft.testlum.testing.framework.context.AliasToStorageOperation;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SystemDataStoreCleaner {

    public void clearAll(final AliasToStorageOperation aliasToStorageOperation) {
        Map<String, AbstractStorageOperation> metadataMap = aliasToStorageOperation.getAlias();
        for (final AbstractStorageOperation AbstractStorageOperation : metadataMap.values()) {
            AbstractStorageOperation.clearSystem();
        }
    }
}

package com.knubisoft.testlum.testing.framework;

import com.knubisoft.testlum.testing.framework.context.AliasToStorageOperation;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.exception.IntegrationDisabledException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class SystemDataStoreCleaner {

    public void clearAll(final AliasToStorageOperation aliasToStorageOperation) {
        Map<String, AbstractStorageOperation> metadataMap = aliasToStorageOperation.getAlias();
        for (final AbstractStorageOperation abstractStorageOperation : metadataMap.values()) {
            try {
                abstractStorageOperation.clearSystem();
            } catch (IntegrationDisabledException e) {
                log.info("Unable to perform operation {} because integration is disabled", abstractStorageOperation);
            }
        }
    }
}

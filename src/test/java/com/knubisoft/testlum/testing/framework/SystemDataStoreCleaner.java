package com.knubisoft.testlum.testing.framework;

import com.knubisoft.testlum.testing.framework.context.NameToAdapterAlias;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SystemDataStoreCleaner {

    public void clearAll(final NameToAdapterAlias nameToAdapterAlias) {
        Map<String, NameToAdapterAlias.Metadata> metadataMap = nameToAdapterAlias.getAlias();
        for (final NameToAdapterAlias.Metadata metadata : metadataMap.values()) {
            metadata.getStorageOperation().clearSystem();
        }
    }
}

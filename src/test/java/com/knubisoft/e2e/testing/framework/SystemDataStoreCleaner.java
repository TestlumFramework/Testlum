package com.knubisoft.e2e.testing.framework;

import com.knubisoft.e2e.testing.framework.context.NameToAdapterAlias;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemDataStoreCleaner {

    public void cleanAll(final NameToAdapterAlias nameToAdapterAlias) {
        Map<String, NameToAdapterAlias.Metadata> metadataMap = nameToAdapterAlias.getAlias();
        for (final Map.Entry<String, NameToAdapterAlias.Metadata> entry : metadataMap.entrySet()) {
            entry.getValue().getStorageOperation().clearSystem();
        }
    }
}

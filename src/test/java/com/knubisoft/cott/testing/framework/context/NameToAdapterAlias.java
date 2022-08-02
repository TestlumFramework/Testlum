package com.knubisoft.cott.testing.framework.context;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.db.StorageOperation;
import com.knubisoft.cott.testing.framework.util.LogMessage;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RequiredArgsConstructor
public class NameToAdapterAlias {

    private final Map<String, Metadata> alias;

    public Metadata getByNameOrThrow(final String name) {
        String upperName = name.toUpperCase(Locale.US);
        Metadata metadata = this.alias.get(upperName);
        if (metadata == null) {
            throw new DefaultFrameworkException(LogMessage.ALIAS_BY_STORAGE_NAME_NOT_FOUND, upperName, alias.keySet());
        }
        return metadata;
    }

    public Map<String, Metadata> getAlias() {
        return new HashMap<>(alias);
    }

    public void removeAlias(final String name) {
        this.alias.remove(name);
    }

    @Builder
    @Getter
    public static class Metadata {
        private final Object configuration;
        private final StorageOperation storageOperation;
    }
}

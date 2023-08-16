package com.knubisoft.testlum.testing.framework.context;

import com.knubisoft.testlum.testing.framework.db.StorageOperation;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.ALIAS_BY_STORAGE_NAME_NOT_FOUND;

@RequiredArgsConstructor
public class NameToAdapterAliasImpl implements NameToAdapterAlias {

    private final Map<String, NameToAdapterAlias.Metadata> alias;

    public NameToAdapterAlias.Metadata getByNameOrThrow(final String name) {
        String adapterName = name.toUpperCase(Locale.US);
        NameToAdapterAlias.Metadata metadata = this.alias.get(adapterName);
        if (Objects.isNull(metadata)) {
            throw new DefaultFrameworkException(ALIAS_BY_STORAGE_NAME_NOT_FOUND, adapterName, alias.keySet());
        }
        return metadata;
    }

    public Map<String, NameToAdapterAlias.Metadata> getAlias() {
        return Collections.unmodifiableMap(alias);
    }

    @Builder
    @Getter
    public static class Metadata implements NameToAdapterAlias.Metadata {
        private final Object configuration;
        private final StorageOperation storageOperation;
    }
}

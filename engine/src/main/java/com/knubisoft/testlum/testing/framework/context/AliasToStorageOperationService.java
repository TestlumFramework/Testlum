package com.knubisoft.testlum.testing.framework.context;

import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.*;

@RequiredArgsConstructor
@Service
public class AliasToStorageOperationService implements AliasToStorageOperation {

    private final List<AliasAdapter> aliasAdapters;

    @Getter
    private Map<String, AbstractStorageOperation> alias;

    @PostConstruct
    public void init() {
        Map<String, AbstractStorageOperation> aliasMap = new LinkedCaseInsensitiveMap<>(aliasAdapters.size());
        for (AliasAdapter aliasAdapter : aliasAdapters) {
            aliasAdapter.apply(aliasMap);
        }
        this.alias = Collections.unmodifiableMap(aliasMap);
    }

    public AbstractStorageOperation getByNameOrThrow(final String name) {
        String adapterName = name.toUpperCase(Locale.US);
        AbstractStorageOperation storageOperation = this.alias.get(adapterName);
        if (Objects.isNull(storageOperation)) {
            final String msg = ExceptionMessage.ALIAS_BY_STORAGE_NAME_NOT_FOUND;
            throw new DefaultFrameworkException(msg, adapterName, alias.keySet());
        }
        return storageOperation;
    }

}

package com.knubisoft.testlum.testing.framework.context;

import com.knubisoft.testlum.testing.framework.db.StorageOperation;

import java.util.Map;

public interface AliasAdapter {

    void apply(Map<String, StorageOperation> aliasMap);

}

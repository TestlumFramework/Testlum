package com.testlum.testing.framework.context;

import com.testlum.testing.framework.db.AbstractStorageOperation;

import java.util.Map;

public interface AliasAdapter {

    void apply(Map<String, AbstractStorageOperation> aliasMap);

}

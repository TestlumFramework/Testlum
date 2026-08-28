package com.testlum.testing.framework.context;

import com.testlum.testing.framework.db.AbstractStorageOperation;

import java.util.Map;

public interface AliasToStorageOperation {

    AbstractStorageOperation getByNameOrThrow(String name);
    Map<String, AbstractStorageOperation> getAlias();
}

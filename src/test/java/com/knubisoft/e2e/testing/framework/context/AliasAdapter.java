package com.knubisoft.e2e.testing.framework.context;

import java.util.Map;

public interface AliasAdapter {

    void apply(Map<String, NameToAdapterAlias.Metadata> aliasMap);

}

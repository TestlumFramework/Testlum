package com.knubisoft.cott.testing.framework.context;

import java.util.Map;

public interface AliasAdapter {

    void apply(Map<String, NameToAdapterAlias.Metadata> aliasMap);

}

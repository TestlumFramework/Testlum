package com.knubisoft.cott.testing.framework.env;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class AliasEnv {

    private final String alias;
    private final String environment;

    public static AliasEnv build(final String alias) {
        return new AliasEnv(alias, EnvManager.currentEnv());
    }
}

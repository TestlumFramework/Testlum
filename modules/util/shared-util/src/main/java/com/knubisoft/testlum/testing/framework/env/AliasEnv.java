package com.knubisoft.testlum.testing.framework.env;

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
}

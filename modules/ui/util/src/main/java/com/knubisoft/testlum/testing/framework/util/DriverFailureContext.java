package com.knubisoft.testlum.testing.framework.util;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DriverFailureContext {

    private final UiDriverKind kind;
    private final String alias;
    private final String qualifier;
    private final String env;
    private final String configPath;
    private final String connectionName;
    private final String serverUrl;
}

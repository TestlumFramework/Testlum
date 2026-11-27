package com.testlum.testing.framework.util;

import com.testlum.testing.framework.constant.DriverFailureMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UiDriverKind {

    WEB("web", false, DriverFailureMessage.FALLBACK_WEB),
    MOBILEBROWSER("mobilebrowser", true, DriverFailureMessage.FALLBACK_MOBILEBROWSER),
    NATIVE("native", true, DriverFailureMessage.FALLBACK_NATIVE);

    private final String configElement;
    private final boolean device;
    private final String fallbackHint;
}

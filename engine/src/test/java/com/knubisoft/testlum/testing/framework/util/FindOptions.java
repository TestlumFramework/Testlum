package com.knubisoft.testlum.testing.framework.util;

import lombok.Builder;
import lombok.Getter;

import java.time.Duration;

@Getter
@Builder
public class FindOptions {

    public enum State { PRESENT, VISIBLE, CLICKABLE }

    private final Duration maxTime;

    @Builder.Default
    private final Duration pollInterval = Duration.ofMillis(500);

    @Builder.Default
    private final boolean useConfigSleep = true;

    @Builder.Default
    private final boolean waitForDomComplete = true;

    @Builder.Default
    private final State state = State.PRESENT;

    public static FindOptions defaults() {
        return FindOptions.builder().build();
    }
}
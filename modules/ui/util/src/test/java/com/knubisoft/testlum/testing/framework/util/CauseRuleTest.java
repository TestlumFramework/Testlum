package com.knubisoft.testlum.testing.framework.util;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CauseRuleTest {

    @Test
    void extractsNothingWhenThereIsNoRule() {
        assertTrue(CauseRule.none().apply("anything at all").isEmpty());
    }

    @Test
    void extractsNothingWhenThePatternDoesNotMatch() {
        assertTrue(CauseRule.of("never present").apply("some driver failure").isEmpty());
    }

    @Test
    void extractsNothingFromABlankMessage() {
        assertTrue(CauseRule.of("boom").apply("   ").isEmpty());
    }

    @Test
    void ignoresCaseInEveryBranchOfAnAlternation() {
        CauseRule rule = CauseRule.of("no such file or directory|connection refused");

        assertEquals(Optional.of("Connection Refused"), rule.apply("socket error: Connection Refused"));
        assertEquals(Optional.of("No Such File Or Directory"), rule.apply("No Such File Or Directory"));
    }

    @Test
    void letsTheDotSpanLineBreaks() {
        CauseRule rule = CauseRule.of("start.*?stop");

        assertEquals(Optional.of("start\nstop"), rule.apply("start\nstop"));
    }

    @Test
    void returnsTheWholeMatchWhenNoTemplateIsGiven() {
        Optional<String> cause = CauseRule.of("activity class \\{[^}]+} does not exist")
                .apply("Error: Activity class {com.app/.Main} does not exist.");

        assertEquals(Optional.of("Activity class {com.app/.Main} does not exist"), cause);
    }

    @Test
    void fillsTheTemplateFromCaptureGroups() {
        Optional<String> cause = CauseRule.of("start the '([^']+)'.*?after (\\d+)ms", "%s stalled after %sms")
                .apply("Cannot start the 'com.app' application\nOriginal error: timed out after 20000ms");

        assertEquals(Optional.of("com.app stalled after 20000ms"), cause);
    }
}

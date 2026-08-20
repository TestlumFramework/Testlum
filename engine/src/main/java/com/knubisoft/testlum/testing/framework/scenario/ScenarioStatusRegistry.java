package com.knubisoft.testlum.testing.framework.scenario;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ScenarioStatusRegistry {

    private static final String SCENARIOS_FOLDER_NAME = "scenarios";

    private static final Map<String, String> INVALID = new ConcurrentHashMap<>();
    private static final Map<String, String> SKIPPED = new ConcurrentHashMap<>();

    private ScenarioStatusRegistry() {
    }

    public static void registerInvalid(final File scenarioFile, final String reason) {
        INVALID.put(shortenScenarioPath(scenarioFile.getPath()), reason);
    }

    public static void registerSkipped(final File scenarioFile, final String reason) {
        SKIPPED.put(shortenScenarioPath(scenarioFile.getPath()), reason);
    }

    public static Map<String, String> getInvalid() {
        return Collections.unmodifiableMap(INVALID);
    }

    public static Map<String, String> getSkipped() {
        return Collections.unmodifiableMap(SKIPPED);
    }

    public static void clear() {
        INVALID.clear();
        SKIPPED.clear();
    }

    private static String shortenScenarioPath(final String scenarioPath) {
        String shortened = StringUtils.substringAfter(scenarioPath, SCENARIOS_FOLDER_NAME);
        return StringUtils.defaultIfEmpty(shortened, scenarioPath);
    }
}

package com.testlum.testing.framework.mcp;

import com.testlum.testing.framework.mcp.constant.ToolMessage;
import com.testlum.testing.framework.mcp.exception.ToolException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

/** The folder layout the engine demands, stated once. */
final class Layout {

    static final String SCENARIOS = "scenarios";
    static final String DATA = "data";
    static final String CONFIG = "config";
    static final String LOCATORS = "locators";
    static final String SCENARIO_FILE = "scenario.xml";
    static final String INTEGRATION_FILE = "integration.xml";
    static final String GLOBAL_CONFIG = "global-config.xml";
    static final String REPORT = "report";
    static final String JUNIT_FILE = "junit.xml";

    private Layout() {
    }

    /**
     * Names are always compared ignoring case. The file system may be case-insensitive even when the JVM runs on
     * Linux (a Windows folder mounted into the container), and treating two names as one only ever blocks more.
     */
    static boolean sameName(final String a, final String b) {
        return a.equalsIgnoreCase(b);
    }

    static boolean startsWithName(final String name, final String prefix) {
        return name.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    /** True when the path exists, or when the same path with different letter case does. */
    static boolean existsAnyCase(final Path root, final Path target) {
        Path current = root;
        for (Path part : root.relativize(target)) {
            current = child(current, part.toString());
            if (current == null) {
                return false;
            }
        }
        return true;
    }

    private static Path child(final Path parent, final String name) {
        if (Files.exists(parent.resolve(name))) {
            return parent.resolve(name);
        }
        try (Stream<Path> siblings = Files.list(parent)) {
            return siblings.filter(f -> f.getFileName().toString().equalsIgnoreCase(name)).findFirst().orElse(null);
        } catch (IOException e) {
            return null;
        }
    }

    /** Every scenario under scenarios/, by its path under that folder, which is how an include names it. */
    static Map<String, Path> scenarioFiles(final Path root) {
        Path dir = root.resolve(SCENARIOS);
        if (!Files.isDirectory(dir)) {
            throw new ToolException(ToolMessage.NO_SCENARIOS_FOLDER, root);
        }
        Map<String, Path> out = new LinkedHashMap<>();
        try (Stream<Path> s = Files.walk(dir)) {
            s.filter(f -> f.getFileName().toString().equals(SCENARIO_FILE)).sorted()
                    .forEach(f -> out.put(dir.relativize(f.getParent()).toString().replace('\\', '/'), f));
        } catch (IOException e) {
            throw new ToolException(e, ToolMessage.CANNOT_LIST_SCENARIOS, e.getMessage());
        }
        return out;
    }
}

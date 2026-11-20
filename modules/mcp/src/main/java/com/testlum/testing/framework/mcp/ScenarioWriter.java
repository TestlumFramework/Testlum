package com.testlum.testing.framework.mcp;

import com.testlum.testing.framework.mcp.constant.ToolMessage;
import com.testlum.testing.framework.mcp.exception.ToolException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/** Writes a scenario with its data files, after the engine schema accepts it. */
@RequiredArgsConstructor
@Component
final class ScenarioWriter {

    private static final Pattern FILE_NAME = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]*");
    private static final Pattern CSV_FILE = Pattern.compile("[A-Za-z0-9_-]+\\.csv");
    private static final String DATA_PREFIX = Layout.DATA + "/";

    private final EngineSchemas schemas;
    private final ScenarioCatalog catalog;

    /** Files are keyed by name and land next to scenario.xml; "data/name.csv" lands in the data folder. */
    String writeTest(final Path root, final String name, final String xml, final JsonNode files,
                     final boolean overwrite) {
        if (!ScenarioCatalog.NAME.matcher(name).matches()) {
            throw new ToolException(ToolMessage.BAD_SCENARIO_NAME);
        }
        Map<Path, String> content = contentOf(root, root.resolve(Layout.SCENARIOS).resolve(name), xml, files);
        requireValid(xml);
        requireFree(root, content.keySet(), overwrite);
        requireUnshared(root, name, content.keySet());
        FileSwap.apply(content);
        return "Wrote scenarios/" + name + ": " + content.keySet().stream()
                .map(p -> root.relativize(p).toString().replace('\\', '/')).toList();
    }

    private static Map<Path, String> contentOf(final Path root, final Path folder, final String xml,
                                               final JsonNode files) {
        Map<Path, String> content = new LinkedHashMap<>();
        if (files != null && files.isObject()) {
            files.properties().forEach(e -> content.put(target(root, folder, e.getKey()), e.getValue().asText()));
        }
        content.put(folder.resolve(Layout.SCENARIO_FILE), xml);
        return content;
    }

    private static Path target(final Path root, final Path folder, final String key) {
        if (key.startsWith(DATA_PREFIX) && CSV_FILE.matcher(key.substring(DATA_PREFIX.length())).matches()) {
            return root.resolve(Layout.DATA).resolve(key.substring(DATA_PREFIX.length()));
        }
        if (!FILE_NAME.matcher(key).matches() || key.equals(Layout.SCENARIO_FILE)) {
            throw new ToolException(ToolMessage.BAD_FILE_NAME, key);
        }
        return folder.resolve(key);
    }

    private void requireValid(final String xml) {
        Path scratch = null;
        ToolException failure = null;
        try {
            scratch = Files.createTempFile("testlum-mcp-", ".xml");
            Files.writeString(scratch, xml, StandardCharsets.UTF_8);
            report(schemas.validate("scenario.xsd", scratch, Layout.SCENARIO_FILE));
        } catch (IOException e) {
            failure = new ToolException(e, ToolMessage.CANNOT_STAGE, e.getMessage());
        } catch (ToolException e) {
            failure = e;
        }
        throwFirst(failure, removed(scratch));
    }

    /** The scratch file is always removed; a failure to remove it is reported, next to the real failure if any. */
    private static IOException removed(final Path scratch) {
        try {
            if (scratch != null) {
                Files.deleteIfExists(scratch);
            }
            return null;
        } catch (IOException e) {
            return new IOException(scratch + ": " + e.getMessage(), e);
        }
    }

    private static void throwFirst(final ToolException failure, final IOException leftover) {
        if (failure != null) {
            if (leftover != null) {
                failure.addSuppressed(leftover);
            }
            throw failure;
        }
        if (leftover != null) {
            throw new ToolException(leftover, ToolMessage.TEMP_NOT_REMOVED, leftover.getMessage());
        }
    }

    private void report(final List<EngineSchemas.Issue> bad) {
        if (!bad.isEmpty()) {
            throw new ToolException(ToolMessage.NOT_VALID, schemas.namespaceOf("scenario"),
                    String.join("\n", bad.stream().map(EngineSchemas.Issue::toString).toList()));
        }
    }

    private static void requireFree(final Path root, final Iterable<Path> targets, final boolean overwrite) {
        for (Path p : targets) {
            if (!overwrite && Layout.existsAnyCase(root, p)) {
                throw new ToolException(ToolMessage.ALREADY_EXISTS, root.relativize(p).toString().replace('\\', '/'));
            }
        }
    }

    /** A csv another scenario reads, or might read because that scenario cannot be read, is not ours to replace. */
    private void requireUnshared(final Path root, final String name, final Iterable<Path> targets) {
        Path data = root.resolve(Layout.DATA);
        for (Path p : targets) {
            if (p.getParent().equals(data) && Layout.existsAnyCase(root, p)) {
                ScenarioCatalog.CsvReaders readers = catalog.readersOf(root, p.getFileName().toString(), name);
                requireNone(p, readers);
            }
        }
    }

    private static void requireNone(final Path csv, final ScenarioCatalog.CsvReaders readers) {
        if (!readers.unreadable().isEmpty()) {
            throw new ToolException(ToolMessage.CSV_UNVERIFIED, csv.getFileName(),
                    String.join(", ", readers.unreadable()));
        }
        if (!readers.users().isEmpty()) {
            throw new ToolException(ToolMessage.CSV_IN_USE, csv.getFileName(), String.join(", ", readers.users()));
        }
    }
}

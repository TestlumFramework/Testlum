package com.testlum.testing.framework.report;

import com.testlum.testing.framework.constant.LogMessage;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.TestResourceSettings;
import com.testlum.testing.framework.scenario.ScenarioStatusRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Writes scenario results as JUnit XML (one testsuite, one testcase per scenario)
 * so CI tools such as Azure DevOps PublishTestResults can read them from the fixed path report/junit.xml.
 * Invalid scenarios, which never ran, are written as errors so a report cannot look green with
 * scenarios missing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JUnitReportGenerator implements ReportGenerator {

    private static final double MS_PER_SECOND = 1000.0;

    private static final String FILE_NAME = "junit.xml";

    private final TestResourceSettings settings;

    /**
     * Removes the report of an earlier run. It is called before the run starts and throws when the file cannot be
     * removed, so a report found after a run was written by that run.
     */
    public void removeOldReport() {
        Path file = reportFile();
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.error(LogMessage.UNABLE_TO_REMOVE_OLD_JUNIT_REPORT, file, e);
            throw new DefaultFrameworkException(
                    LogMessage.UNABLE_TO_REMOVE_OLD_JUNIT_REPORT.replace("{}", file.toString()), e);
        }
    }

    /** Writes report/junit.xml through a temporary file, so a failed write leaves no partial report at the path. */
    @Override
    public void generateReport(final GlobalScenarioStatCollector globalScenarioStatCollector) {
        Path file = reportFile();
        String xml = toXml(globalScenarioStatCollector.getResults(), ScenarioStatusRegistry.getSkipped(),
                ScenarioStatusRegistry.getInvalid());
        Path temp = null;
        try {
            Files.createDirectories(file.getParent());
            temp = Files.createTempFile(file.getParent(), FILE_NAME, ".tmp");
            Files.writeString(temp, xml, StandardCharsets.UTF_8);
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw writeFailure(file, temp, e);
        }
    }

    private static DefaultFrameworkException writeFailure(final Path file, final Path temp, final IOException cause) {
        log.error(LogMessage.UNABLE_TO_WRITE_JUNIT_REPORT, file, cause);
        DefaultFrameworkException failure = new DefaultFrameworkException(
                LogMessage.UNABLE_TO_WRITE_JUNIT_REPORT.replace("{}", file.toString()), cause);
        discard(temp, failure);
        return failure;
    }

    private static void discard(final Path temp, final Exception failure) {
        if (temp == null) {
            return;
        }
        try {
            Files.deleteIfExists(temp);
        } catch (IOException e) {
            failure.addSuppressed(e);
        }
    }

    private Path reportFile() {
        return settings.getTestResourcesFolder().toPath().resolve(TestResourceSettings.REPORT_FOLDER)
                .resolve(FILE_NAME);
    }

    /** Builds the report text; skipped and invalid map a scenario path to the reason. */
    static String toXml(final List<ScenarioResult> results, final Map<String, String> skipped,
                        final Map<String, String> invalid) {
        long failures = results.stream().filter(r -> !r.isSuccess()).count();
        long millis = results.stream().mapToLong(ScenarioResult::getExecutionTime).sum();
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append(String.format("<testsuite name=\"testlum\" tests=\"%d\" failures=\"%d\" errors=\"%d\" "
                        + "skipped=\"%d\" time=\"%s\">%n",
                results.size() + skipped.size() + invalid.size(), failures, invalid.size(), skipped.size(),
                seconds(millis)));
        appendResults(xml, results);
        skipped.forEach((path, reason) -> appendSkipped(xml, path, reason));
        invalid.forEach((path, reason) -> appendInvalid(xml, path, reason));
        return xml.append("</testsuite>\n").toString();
    }

    // A scenario that runs more than once (variation rows, environments) gets a number so no two names match.
    private static void appendResults(final StringBuilder xml, final List<ScenarioResult> results) {
        Map<String, Long> runs = results.stream()
                .collect(Collectors.groupingBy(ScenarioResult::getPath, Collectors.counting()));
        Map<String, Integer> seen = new HashMap<>();
        for (ScenarioResult r : results) {
            int n = seen.merge(r.getPath(), 1, Integer::sum);
            appendResult(xml, r, runs.get(r.getPath()) > 1 ? folderOf(r.getPath()) + " #" + n : folderOf(r.getPath()));
        }
    }

    private static void appendSkipped(final StringBuilder xml, final String path, final String reason) {
        xml.append(String.format(
                "  <testcase name=\"%s\" classname=\"%s\" time=\"0.000\"><skipped message=\"%s\"/></testcase>%n",
                esc(folderOf(path)), esc(path), esc(reason)));
    }

    private static void appendInvalid(final StringBuilder xml, final String path, final String reason) {
        xml.append(String.format(
                "  <testcase name=\"%s\" classname=\"%s\" time=\"0.000\"><error message=\"%s\">%s</error>"
                        + "</testcase>%n",
                esc(folderOf(path)), esc(path), esc(reason.lines().findFirst().orElse("")), esc(reason)));
    }

    private static void appendResult(final StringBuilder xml, final ScenarioResult r, final String name) {
        xml.append(String.format("  <testcase name=\"%s\" classname=\"%s\" time=\"%s\"",
                esc(name), esc(r.getPath()), seconds(r.getExecutionTime())));
        if (r.isSuccess()) {
            xml.append("/>\n");
            return;
        }
        String cause = Objects.toString(r.getCause(), "");
        xml.append(String.format("><failure message=\"%s\">%s</failure></testcase>%n",
                esc(cause.lines().findFirst().orElse("")), esc(cause)));
    }

    // The scenario folder is the stable identity; the file name is always scenario.xml.
    private static String folderOf(final String path) {
        Path parent = Paths.get(path).getParent();
        return parent == null ? path : parent.getFileName().toString();
    }

    private static String seconds(final long millis) {
        return String.format(java.util.Locale.ROOT, "%.3f", millis / MS_PER_SECOND);
    }

    // Stack traces can hold control characters that are illegal in XML 1.0.
    private static String esc(final String text) {
        return text.replaceAll("[^\\u0009\\u000A\\u000D\\u0020-\\uD7FF\\uE000-\\uFFFD]", "")
                .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}

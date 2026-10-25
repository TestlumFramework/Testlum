package com.testlum.testing.framework.mcp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.testlum.testing.framework.mcp.exception.ToolException;
import com.testlum.testing.framework.mcp.constant.ToolMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/** Runs the engine jar headless and turns its exit code and report/junit.xml into a short report. */
@Component
final class EngineRunner {

    private static final int TAIL_LINES = 40;
    private static final long KILL_WAIT_SECONDS = 10;
    private static final long PUMP_WAIT_MILLIS = 2000;
    private static final Pattern ANSI = Pattern.compile("\u001B\\[[0-9;?]*[ -/]*[@-~]");
    private static final String NO_RESULTS = "report/junit.xml is missing or holds no scenario runs, so nothing is "
            + "known to have run (the global config needs <junit enabled=\"true\"/> in its report).\n";
    private static final Map<Integer, String> EXIT = Map.of(
            0, "all scenarios passed",
            1, "at least one scenario failed",
            2, "no tests found (skipped silently, treat as a failure)",
            3, "invalid config or folder layout",
            4, "tests failed to load (skipped silently, treat as a failure)");

    private final String engineJar;

    EngineRunner(@Value("${TESTLUM_ENGINE_JAR:}") final String engineJar) {
        this.engineJar = engineJar;
    }

    /** What one engine run is asked to do. */
    record Job(Path dir, String config, String tags, int timeoutSeconds) { }

    private record Result(int exit, boolean timedOut, List<String> tail) { }

    String run(final Job job) {
        Path root = requireReady(job);
        try {
            Files.deleteIfExists(root.resolve(Layout.REPORT).resolve(Layout.JUNIT_FILE));
            return report(root, exec(root, job));
        } catch (ToolException e) {
            throw e;
        } catch (Exception e) {
            throw new ToolException(e, ToolMessage.RUN_FAILED, e);
        }
    }

    private Path requireReady(final Job job) {
        if (engineJar == null || engineJar.isBlank() || !Files.isRegularFile(Path.of(engineJar))) {
            throw new ToolException(ToolMessage.ENGINE_JAR_NOT_SET);
        }
        Path root = job.dir().toAbsolutePath().normalize();
        if (!Files.isRegularFile(root.resolve(job.config()))) {
            throw new ToolException(ToolMessage.NO_CONFIG, job.config(), root);
        }
        return root;
    }

    private Result exec(final Path root, final Job job) throws Exception {
        Process p = start(root, job);
        Deque<String> tail = new ArrayDeque<>();
        Thread pump = startPump(p, tail);
        boolean done = p.waitFor(Math.max(1, job.timeoutSeconds()), TimeUnit.SECONDS);
        if (!done) {
            kill(p);
        }
        pump.join(PUMP_WAIT_MILLIS);
        synchronized (tail) {
            return new Result(done ? p.exitValue() : -1, !done, new ArrayList<>(tail));
        }
    }

    private Process start(final Path root, final Job job) throws IOException {
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        List<String> command = new ArrayList<>(List.of(java, "-jar", engineJar, "--config=" + job.config(),
                "--path=" + root.toString().replace('\\', '/')));
        if (!job.tags().isEmpty()) {
            command.add("--tags=" + job.tags());
        }
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(root.toFile());
        pb.redirectErrorStream(true);
        pb.environment().put("LC_ALL", "C.UTF-8");
        return pb.start();
    }

    private static void kill(final Process p) throws InterruptedException {
        p.descendants().forEach(ProcessHandle::destroyForcibly);
        p.destroyForcibly();
        p.waitFor(KILL_WAIT_SECONDS, TimeUnit.SECONDS);
    }

    private static Thread startPump(final Process p, final Deque<String> tail) {
        Thread pump = new Thread(() -> pump(p, tail), "engine-output");
        pump.setDaemon(true);
        pump.start();
        return pump;
    }

    private static void pump(final Process p, final Deque<String> tail) {
        try (var br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                synchronized (tail) {
                    tail.addLast(line);
                    if (tail.size() > TAIL_LINES) {
                        tail.removeFirst();
                    }
                }
            }
        } catch (IOException ignored) {
            // process ended
        }
    }

    private String report(final Path root, final Result r) throws Exception {
        JunitResults results = JunitResults.read(root.resolve(Layout.REPORT).resolve(Layout.JUNIT_FILE));
        StringBuilder sb = new StringBuilder(r.timedOut() ? "TIMEOUT: engine killed\n" : headline(r.exit(), results));
        sb.append(results.isEmpty() ? NO_RESULTS : results.summary());
        if (r.exit() != 0 || results.isEmpty()) {
            sb.append("--- last ").append(r.tail().size()).append(" console lines ---\n");
            r.tail().forEach(l -> sb.append(stripAnsi(l)).append('\n'));
        }
        return sb.toString().stripTrailing();
    }

    /** Exit zero alone never means passed: the report has to show runs, and none of them failed. */
    private static String headline(final int exit, final JunitResults results) {
        String meaning = EXIT.getOrDefault(exit, "unknown");
        if (exit == 0 && results.executed() == 0) {
            meaning = "FAIL, the report shows no scenario that ran";
        } else if (exit == 0 && results.failed() > 0) {
            meaning = "FAIL, the report holds failed scenarios";
        }
        return "exit " + exit + ": " + meaning + "\n";
    }

    /** Removes terminal color and cursor codes from console lines. */
    static String stripAnsi(final String line) {
        return ANSI.matcher(line).replaceAll("");
    }
}

package com.testlum.testing.framework.mcp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Stands in for the engine jar in runner tests: writes report/junit.xml and exits the way the config asks. */
public final class FakeEngine {

    private static final String PASS = "<testcase name=\"a\" classname=\"scenarios/a/scenario.xml\"/>";
    private static final String FAIL = "<testcase name=\"b\" classname=\"scenarios/b/scenario.xml\">"
            + "<failure message=\"m\">full text</failure></testcase>";
    private static final String ERROR = "<testcase name=\"c\" classname=\"scenarios/c/scenario.xml\">"
            + "<error message=\"boom\">stack trace</error></testcase>";
    private static final String SKIPPED = "<testcase name=\"d\" classname=\"scenarios/d/scenario.xml\">"
            + "<skipped message=\"s\"/></testcase>";

    private static final String SKIPPED_FAILED = "<testcase name=\"e\" classname=\"scenarios/e/scenario.xml\">"
            + "<skipped message=\"s\"/><failure message=\"f\">both</failure></testcase>";

    private static final int CRASH_CODE = 3;
    private static final long HANG_MILLIS = 60_000L;

    private FakeEngine() {
    }

    public static void main(final String[] args) throws IOException, InterruptedException {
        String path = value(args, "--path=");
        String config = value(args, "--config=");
        Files.writeString(Path.of(path, "args.txt"), String.join(" ", args));
        switch (config) {
            case "crash.xml" -> exit("\u001B[31mboom\u001B[0m", CRASH_CODE);
            case "silent.xml" -> exit("nothing written", 0);
            case "hang.xml" -> Thread.sleep(HANG_MILLIS);
            case "tagged.xml" -> report(path, value(args, "--tags=").equals("bad") ? ERROR : PASS);
            default -> reportFor(config, path);
        }
    }

    private static void reportFor(final String config, final String path) throws IOException {
        switch (config) {
            case "skipped.xml" -> report(path, SKIPPED, 0);
            case "empty.xml" -> report(path, "", 0);
            case "lying.xml" -> report(path, PASS + FAIL, 0);
            case "suite.xml" -> write(path, "<testsuite failures=\"2\">" + PASS + "</testsuite>", 0);
            case "both.xml" -> write(path, "<testsuite>" + SKIPPED_FAILED + PASS + "</testsuite>", 1);
            case "nested.xml" -> write(path, "<testsuite><system-out>" + PASS + "</system-out></testsuite>", 0);
            default -> report(path, PASS + FAIL + ERROR + SKIPPED);
        }
    }

    private static void report(final String path, final String cases) throws IOException {
        report(path, cases, cases.contains("<failure") || cases.contains("<error") ? 1 : 0);
    }

    private static void report(final String path, final String cases, final int code) throws IOException {
        write(path, "<testsuite>" + cases + "</testsuite>", code);
    }

    private static void write(final String path, final String xml, final int code) throws IOException {
        Path folder = Path.of(path, "report");
        Files.createDirectories(folder);
        Files.writeString(folder.resolve("junit.xml"), xml);
        exit("done", code);
    }

    private static void exit(final String line, final int code) {
        System.out.println(line);
        System.exit(code);
    }

    private static String value(final String[] args, final String prefix) {
        for (String a : args) {
            if (a.startsWith(prefix)) {
                return a.substring(prefix.length());
            }
        }
        return "";
    }
}

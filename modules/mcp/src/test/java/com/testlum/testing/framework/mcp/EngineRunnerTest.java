package com.testlum.testing.framework.mcp;

import com.testlum.testing.framework.mcp.exception.ToolException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EngineRunnerTest {

    private static final String CLASS_ENTRY = "com/testlum/testing/framework/mcp/FakeEngine.class";

    @TempDir
    Path tmp;

    private EngineRunner runner;
    private Path tests;

    @BeforeEach
    void setUp() throws IOException {
        Path jar = tmp.resolve("fake-engine.jar");
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, FakeEngine.class.getName());
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jar), manifest);
             InputStream in = FakeEngine.class.getResourceAsStream("FakeEngine.class")) {
            out.putNextEntry(new JarEntry(CLASS_ENTRY));
            in.transferTo(out);
        }
        runner = new EngineRunner(jar.toString());
        tests = Files.createDirectories(tmp.resolve("tests"));
        for (String config : new String[] {"global-config.xml", "crash.xml", "silent.xml", "hang.xml", "tagged.xml",
            "skipped.xml", "empty.xml", "lying.xml", "suite.xml", "both.xml", "nested.xml"}) {
            Files.writeString(tests.resolve(config), "<x/>");
        }
    }

    private String run(final String config, final String tags, final int timeoutSeconds) {
        return runner.run(new EngineRunner.Job(tests, config, tags, timeoutSeconds));
    }

    @Test
    void reportsEachScenarioAndTreatsAnErrorAsAFailure() {
        String out = run("global-config.xml", "", 60);
        assertTrue(out.startsWith("exit 1: at least one scenario failed\n"), out);
        assertTrue(out.contains("scenarios: 4 total, 1 passed, 2 failed, 1 skipped"), out);
        assertTrue(out.contains("PASS a\nFAIL b\nFAIL c\nSKIP d\n"), out);
        assertTrue(out.contains("--- b (scenarios/b/scenario.xml)\nfull text"), out);
        assertTrue(out.contains("--- c (scenarios/c/scenario.xml)\nstack trace"), out);
        assertTrue(out.contains("console lines ---\n") && out.endsWith("\ndone"), out);
    }

    @Test
    void passesTheTagsToTheEngineAndOmitsTheConsoleOnSuccess() throws IOException {
        String out = run("tagged.xml", "good", 60);
        assertTrue(out.startsWith("exit 0: all scenarios passed\nscenarios: 1 total, 1 passed, 0 failed, 0 skipped"),
                out);
        assertFalse(out.contains("console lines"), out);
        assertTrue(Files.readString(tests.resolve("args.txt")).endsWith("--tags=good"));
        run("tagged.xml", "", 60);
        assertFalse(Files.readString(tests.resolve("args.txt")).contains("--tags"));
    }

    @Test
    void neverReadsAJunitFileTheRunDidNotWrite() throws IOException {
        Files.createDirectories(tests.resolve("report"));
        Files.writeString(tests.resolve("report/junit.xml"), "<testsuite><testcase name=\"old\"/></testsuite>");
        String out = run("silent.xml", "", 60);
        assertTrue(out.startsWith("exit 0: FAIL, the report shows no scenario that ran\n"), out);
        assertTrue(out.contains("missing or holds no scenario runs"), out);
        assertFalse(out.contains("PASS old"), out);
        assertTrue(out.contains("nothing written"), out);
    }

    @Test
    void exitZeroIsNeverPassedWithoutRunsOrWithAFailureInTheReport() {
        assertTrue(run("empty.xml", "", 60).startsWith("exit 0: FAIL, the report shows no scenario that ran"));
        assertTrue(run("skipped.xml", "", 60).startsWith("exit 0: FAIL, the report shows no scenario that ran"));
        String lying = run("lying.xml", "", 60);
        assertTrue(lying.startsWith("exit 0: FAIL, the report holds failed scenarios\n"), lying);
        assertTrue(lying.contains("scenarios: 2 total, 1 passed, 1 failed, 0 skipped"), lying);
    }

    @Test
    void theGateCountsOnlyTestcasesOfASuiteAndHonoursWhatTheSuiteDeclares() {
        assertTrue(run("nested.xml", "", 60).startsWith("exit 0: FAIL, the report shows no scenario that ran"));
        String declared = run("suite.xml", "", 60);
        assertTrue(declared.startsWith("exit 0: FAIL, the report holds failed scenarios\n"), declared);
        assertTrue(declared.contains("scenarios: 1 total, 1 passed, 2 failed, 0 skipped"), declared);
    }

    @Test
    void aTestcaseThatIsSkippedAndFailedIsCountedOnceAndPassedNeverGoesNegative() {
        String out = run("both.xml", "", 60);
        assertTrue(out.contains("scenarios: 2 total, 1 passed, 1 failed, 0 skipped"), out);
        assertTrue(out.contains("FAIL e\nPASS"), out);
    }

    @Test
    void explainsTheExitCodeAndStripsColorFromTheConsole() {
        String out = run("crash.xml", "", 60);
        assertTrue(out.startsWith("exit 3: invalid config or folder layout\n"), out);
        assertTrue(out.contains("\nboom"), out);
        assertFalse(out.contains("\u001B"), out);
    }

    @Test
    void killsAnEngineThatRunsTooLong() {
        String out = run("hang.xml", "", 1);
        assertTrue(out.startsWith("TIMEOUT: engine killed"), out);
    }

    @Test
    void refusesToRunWithoutAnEngineJarOrAConfig() {
        assertTrue(assertThrows(ToolException.class,
                () -> new EngineRunner("").run(new EngineRunner.Job(tests, "global-config.xml", "", 5)))
                .getMessage().contains("TESTLUM_ENGINE_JAR"));
        String msg = assertThrows(ToolException.class, () -> run("missing.xml", "", 5)).getMessage();
        assertTrue(msg.contains("missing.xml") && msg.contains("testlum_scaffold"), msg);
    }

    @Test
    void stripsAnsiColorCodes() {
        assertEquals("ERROR scenario failed",
                EngineRunner.stripAnsi("\u001B[31mERROR\u001B[0m scenario \u001B[1;32mfailed\u001B[m"));
    }
}

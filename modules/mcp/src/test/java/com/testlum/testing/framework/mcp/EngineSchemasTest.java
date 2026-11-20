package com.testlum.testing.framework.mcp;

import com.testlum.testing.framework.mcp.exception.ToolException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EngineSchemasTest {

    private static final String HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<scenario xmlns=\"http://www.testlum.com/testing/model/scenario\">\n"
            + "<overview><description>A scenario for the schema test</description><name>t</name></overview>\n";

    private static final String CALL = "<http comment=\"Get a user\"><get endpoint=\"/users\">"
            + "<response code=\"200\"/></get></http>";

    @TempDir
    Path tests;

    private final EngineSchemas schemas = TestSupport.schemas();

    @BeforeEach
    void setUp() {
        new FolderScaffolder().scaffold(tests, "sandbox", "demo", "DEFAULT", "http://localhost:1");
    }

    private void scenario(final String name, final String body) throws IOException {
        Path d = tests.resolve("scenarios").resolve(name);
        Files.createDirectories(d);
        Files.writeString(d.resolve("scenario.xml"), HEAD + body + "\n</scenario>\n");
    }

    private String check(final String only) {
        return schemas.checkFolder(tests, only, "global-config.xml");
    }

    @Test
    void theScaffoldedFilesPassTheEngineSchemas() {
        assertTrue(check("").startsWith("PASS: 0 problems in 0 scenarios"), check(""));
    }

    @Test
    void reportsASchemaProblemWithFileLineAndColumn() throws IOException {
        scenario("bad", "<settings><tags>x</tags></settings><http comment=\"Get a user\"><get endpoint=\"users\">"
                + "<response code=\"200\"/></get></http>");
        scenario("good", "<settings><tags>x</tags></settings>" + CALL);
        String r = check("");
        assertTrue(r.startsWith("FAIL: ") && r.contains(" in 2 scenarios"), r);
        assertTrue(r.matches("(?s).*scenarios/bad/scenario.xml:\\d+:\\d+ \\[error].*endpoint.*"), r);
        assertFalse(r.contains("scenarios/good"), r);
        assertFalse(r.contains("{http://"), "namespaces are stripped from the messages\n" + r);
    }

    @Test
    void checksOneScenarioOrAllOfThem() throws IOException {
        scenario("bad", "<oops/>");
        scenario("good", "<settings><tags>x</tags></settings>" + CALL);
        assertTrue(check("good").startsWith("PASS: 0 problems in 1 scenarios"), check("good"));
        assertTrue(check("bad").startsWith("FAIL"), check("bad"));
        assertTrue(check("ghost").contains("scenarios/ghost [error] No such scenario"), check("ghost"));
    }

    @Test
    void checksTheGlobalConfigAndEveryIntegrationFile() throws IOException {
        Files.writeString(tests.resolve("global-config.xml"), "<globalTestConfiguration><environments>"
                + "<env folder=\"sandbox\" enabled=\"true\"/><env folder=\"ghost\" enabled=\"true\"/>"
                + "<env folder=\"off\" enabled=\"false\"/></environments></globalTestConfiguration>");
        Files.writeString(tests.resolve("config/sandbox/integration.xml"), "<integrations/>");
        String r = check("");
        assertTrue(r.contains("global-config.xml:"), r);
        assertTrue(r.contains("config/sandbox/integration.xml:"), r);
        assertTrue(r.contains("config/ghost/integration.xml [error] Integration file not found"), r);
        assertFalse(r.contains("config/off"), r);
        Files.delete(tests.resolve("global-config.xml"));
        assertTrue(check("").contains("Global config not found"), check(""));
    }

    @Test
    void refusesToWorkWithoutTheSchemaFolder() {
        assertTrue(assertThrows(ToolException.class, () -> new EngineSchemas("").dir())
                .getMessage().contains("TESTLUM_XSD_DIR"));
        assertTrue(assertThrows(ToolException.class, () -> new EngineSchemas(tests.toString()).dir())
                .getMessage().contains("no scenario.xsd"));
    }

    @Test
    void aFolderWithoutScenariosIsReported() throws IOException {
        Files.delete(tests.resolve("scenarios"));
        assertTrue(assertThrows(ToolException.class, () -> check("")).getMessage().contains("No scenarios folder"));
    }
}

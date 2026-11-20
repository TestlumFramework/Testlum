package com.testlum.testing.framework.mcp;

import com.testlum.testing.framework.mcp.exception.ToolException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScenarioWriterTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final String SCENARIO = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<scenario xmlns=\"http://www.testlum.com/testing/model/scenario\">\n"
            + "<overview><description>Get one user back</description><name>t</name></overview>\n"
            + "<settings><variations>users.csv</variations><tags>smoke</tags></settings>\n"
            + "<http comment=\"Get the user by id\"><get endpoint=\"/users/{{id}}\">"
            + "<response code=\"200\" file=\"expected_1.json\"/></get></http>\n</scenario>\n";

    @TempDir
    Path tmp;

    private final ScenarioWriter writer = TestSupport.writer();

    @BeforeEach
    void setUp() {
        new FolderScaffolder().scaffold(tmp, "sandbox", "demo", "DEFAULT", "http://localhost:1");
    }

    private static JsonNode json(final String text) {
        return JSON.readTree(text);
    }

    private String write(final String name, final String files, final boolean overwrite) {
        return writer.writeTest(tmp, name, SCENARIO, files == null ? null : json(files), overwrite);
    }

    private String read(final String relative) throws IOException {
        return Files.readString(tmp.resolve(relative));
    }

    @Test
    void writesTheScenarioItsDataFilesAndTheVariationsCsv() throws IOException {
        String out = write("user", "{\"expected_1.json\":\"{}\",\"data/users.csv\":\"id\\n1\\n2\\n\"}", false);
        assertTrue(out.startsWith("Wrote scenarios/user: "), out);
        assertTrue(out.contains("scenarios/user/expected_1.json") && out.contains("data/users.csv")
                && out.contains("scenarios/user/scenario.xml"), out);
        assertEquals("{}", read("scenarios/user/expected_1.json"));
        assertEquals("id\n1\n2\n", read("data/users.csv"));
        assertEquals(SCENARIO, read("scenarios/user/scenario.xml"));
    }

    @Test
    void writesNestedScenarios() throws IOException {
        write("group/login", null, false);
        assertEquals(SCENARIO, read("scenarios/group/login/scenario.xml"));
    }

    @Test
    void writesNothingWhenTheSchemaRejectsTheScenario() {
        String bad = SCENARIO.replace("endpoint=\"/users/{{id}}\"", "endpoint=\"users\"");
        String msg = assertThrows(ToolException.class,
                () -> writer.writeTest(tmp, "bad", bad, json("{\"data/x.csv\":\"id\"}"), false)).getMessage();
        assertTrue(msg.contains("schema") && msg.contains("endpoint"), msg);
        assertTrue(Files.notExists(tmp.resolve("scenarios/bad")));
        assertTrue(Files.notExists(tmp.resolve("data/x.csv")));
    }

    @Test
    void refusesToReplaceAScenarioOrACsvUnlessOverwriteIsSet() throws IOException {
        write("user", "{\"data/users.csv\":\"id\\n1\\n\"}", false);
        assertTrue(assertThrows(ToolException.class, () -> write("user", null, false)).getMessage()
                .contains("scenarios/user/scenario.xml already exists"));
        assertTrue(assertThrows(ToolException.class,
                () -> write("other", "{\"data/users.csv\":\"id\\n9\\n\"}", false)).getMessage()
                .contains("data/users.csv already exists"));
        assertTrue(Files.notExists(tmp.resolve("scenarios/other")), "nothing is written when a target is taken");
        write("user", "{\"data/users.csv\":\"id\\n2\\n\"}", true);
        assertEquals("id\n2\n", read("data/users.csv"));
    }

    @Test
    void refusesNamesAndFileNamesThatEscapeTheFolder() {
        for (String name : new String[] {"../x", "a//b", "/abs", "a b", ""}) {
            assertThrows(ToolException.class, () -> write(name, null, false), name);
        }
        for (String file : new String[] {"../x.json", "sub/x.json", ".hidden", "scenario.xml", "data/../x.csv",
                "data/x.txt", "data/sub/x.csv"}) {
            assertThrows(ToolException.class, () -> write("ok", "{\"" + file + "\":\"x\"}", false), file);
        }
        assertFalse(Files.exists(tmp.resolve("scenarios/ok")));
    }

    @Test
    void refusesToReplaceACsvAnotherScenarioReads() throws IOException {
        write("user", "{\"expected_1.json\":\"{}\",\"data/users.csv\":\"id\\n1\\n\"}", false);
        String msg = assertThrows(ToolException.class, () -> write("other", "{\"data/users.csv\":\"id\\n9\\n\"}", true))
                .getMessage();
        assertTrue(msg.contains("data/users.csv is read by user"), msg);
        assertEquals("id\n1\n", read("data/users.csv"));
        assertFalse(Files.exists(tmp.resolve("scenarios/other")));
        write("user", "{\"expected_1.json\":\"{}\",\"data/users.csv\":\"id\\n2\\n\"}", true);
        assertEquals("id\n2\n", read("data/users.csv"));
    }

    @Test
    void aCaseVariantOfASharedCsvIsTheSameFile() throws IOException {
        write("user", "{\"expected_1.json\":\"{}\",\"data/users.csv\":\"id\\n1\\n\"}", false);
        String msg = assertThrows(ToolException.class, () -> write("other", "{\"data/USERS.csv\":\"id\\n9\\n\"}", true))
                .getMessage();
        assertTrue(msg.contains("is read by user"), msg);
        assertEquals("id\n1\n", read("data/users.csv"));
        assertFalse(Files.exists(tmp.resolve("data/USERS.csv")));
        assertTrue(assertThrows(ToolException.class, () -> write("third", "{\"data/Users.csv\":\"x\"}", false))
                .getMessage().contains("already exists"));
    }

    @Test
    void aFailureWhileWritingLeavesTheExistingFilesAndNoTempFilesBehind() throws IOException {
        Files.writeString(tmp.resolve("data/users.csv"), "old");
        Files.writeString(tmp.resolve("scenarios/user"), "in the way");
        assertThrows(ToolException.class, () -> write("user", "{\"data/users.csv\":\"new\"}", true));
        assertEquals("old", read("data/users.csv"));
        try (var left = Files.list(tmp.resolve("data"))) {
            assertEquals(java.util.List.of("users.csv"), left.map(f -> f.getFileName().toString()).toList());
        }
    }

    @Test
    void aMoveThatFailsLaterPutsTheEarlierFilesBackAndRemovesTheNewOnes() throws IOException {
        write("user", "{\"expected_1.json\":\"{}\",\"data/users.csv\":\"old\"}", false);
        Files.delete(tmp.resolve("scenarios/user/expected_1.json"));
        // the files go in as: new request, the csv being replaced, expected_1.json (blocked), scenario.xml
        Files.createDirectories(tmp.resolve("scenarios/user/expected_1.json/in-the-way"));

        String msg = assertThrows(ToolException.class, () -> write("user",
                "{\"request_1.json\":\"new\",\"data/users.csv\":\"changed\",\"expected_1.json\":\"{}\"}", true))
                .getMessage();

        assertTrue(msg.startsWith("Cannot write scenario files"), msg);
        assertEquals("old", read("data/users.csv"));
        assertFalse(Files.exists(tmp.resolve("scenarios/user/request_1.json")));
        assertEquals(SCENARIO, read("scenarios/user/scenario.xml"));
        assertTrue(Files.isDirectory(tmp.resolve("scenarios/user/expected_1.json/in-the-way")));
        try (var left = Files.list(tmp.resolve("data"))) {
            assertEquals(java.util.List.of("users.csv"), left.map(f -> f.getFileName().toString()).toList());
        }
        try (var left = Files.list(tmp.resolve("scenarios/user"))) {
            assertTrue(left.noneMatch(f -> f.getFileName().toString().startsWith(".testlum-")));
        }
    }

    @Test
    void refusesToReplaceACsvWhileAScenarioThatCannotBeReadMightUseIt() throws IOException {
        write("user", "{\"expected_1.json\":\"{}\",\"data/users.csv\":\"old\"}", false);
        Path broken = Files.createDirectories(tmp.resolve("scenarios/broken"));
        Files.writeString(broken.resolve("scenario.xml"), "<scenario>");
        String msg = assertThrows(ToolException.class, () -> write("user",
                "{\"expected_1.json\":\"{}\",\"data/users.csv\":\"new\"}", true)).getMessage();
        assertTrue(msg.contains("may be read by a scenario that cannot be read (broken)"), msg);
        assertEquals("old", read("data/users.csv"));
    }

    @Test
    void aScenarioBeingRewrittenDoesNotBlockItsOwnCsvEvenWhenItsOldVersionIsBroken() throws IOException {
        write("user", "{\"expected_1.json\":\"{}\",\"data/users.csv\":\"old\"}", false);
        Files.writeString(tmp.resolve("scenarios/user/scenario.xml"), "<scenario>");
        write("user", "{\"expected_1.json\":\"{}\",\"data/users.csv\":\"new\"}", true);
        assertEquals("new", read("data/users.csv"));
    }
}

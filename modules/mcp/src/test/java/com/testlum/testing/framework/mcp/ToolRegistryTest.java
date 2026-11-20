package com.testlum.testing.framework.mcp;

import com.testlum.testing.framework.mcp.exception.ToolException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolRegistryTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final String LOCAL_URL = "http://localhost:1";
    private static final String SCENARIO = """
            <scenario xmlns="http://www.testlum.com/testing/model/scenario">
                <overview><description>ping answers</description><name>ping</name></overview>
                <settings><tags>smoke</tags></settings>
                <http comment="Call the ping endpoint" alias="DEFAULT">
                    <get endpoint="/ping"><response code="200"/></get>
                </http>
            </scenario>
            """;

    @TempDir
    Path tmp;

    @Nested
    class Lookup {

        @Test
        void describesAttributesAndChildren() {
            EngineSchemas s = TestSupport.schemas();
            String get = s.describe("scenario", "http/get");
            assertTrue(get.contains("name=\"endpoint\"") && get.contains("use=\"required\""), get);
            assertTrue(get.contains("name=\"response\""), get);
            assertFalse(get.contains("xmlns"), "namespace declarations are stripped\n" + get);
            assertTrue(s.describe("scenario", "assert/equal").contains("minLength"));
            assertTrue(s.describe("integration", "apis/api").contains("https?://"));
            assertTrue(s.describe("global-config", "").contains("environments"));
        }

        @Test
        void keepsSameNamesInTwoNamespacesApart() {
            EngineSchemas s = TestSupport.schemas();
            assertTrue(s.describe("scenario", "postgres").contains("name=\"file\""));
            assertTrue(s.describe("scenario", "var").contains("generate"), "children from a named group");
        }

        @Test
        void anUnknownElementShowsTheParentDefinition() {
            EngineSchemas s = TestSupport.schemas();
            assertTrue(assertThrows(ToolException.class, () -> s.describe("scenario", "http/nope"))
                    .getMessage().contains("name=\"get\""));
            assertThrows(ToolException.class, () -> s.describe("bogus", ""));
        }

        @Test
        void theRootTellsWhichNamespaceItMustCarry() {
            EngineSchemas s = TestSupport.schemas();
            assertTrue(s.describe("scenario", "").endsWith("\nxmlns: http://www.testlum.com/testing/model/scenario"));
            assertTrue(s.describe("global-config", "")
                    .endsWith("\nxmlns: http://www.testlum.com/testing/model/global-config"));
            assertTrue(s.describe("scenario", "scenario/http").startsWith("scenario/http\n"));
            assertFalse(s.describe("scenario", "http").contains("xmlns:"));
        }

        @Test
        void aChildThatHasNoChildrenCannotBeDescendedInto() {
            EngineSchemas s = TestSupport.schemas();
            assertTrue(assertThrows(ToolException.class, () -> s.describe("scenario", "http/get/endpoint/x"))
                    .getMessage().contains("endpoint"));
        }
    }

    @Nested
    class Scaffold {

        @Test
        void createsFilesAndKeepsExisting() throws Exception {
            FolderScaffolder sc = new FolderScaffolder();
            String first = sc.scaffold(tmp, "sandbox", "demo", "DEFAULT", "https://example.com");
            assertTrue(first.contains("created global-config.xml"), first);
            Files.writeString(tmp.resolve("global-config.xml"), "mine");
            String second = sc.scaffold(tmp, "sandbox", "demo", "DEFAULT", "https://example.com");
            assertTrue(second.contains("kept    global-config.xml"), second);
            assertEquals("mine", Files.readString(tmp.resolve("global-config.xml")));
        }

        @Test
        void switchesTheJunitReportOn() throws Exception {
            new FolderScaffolder().scaffold(tmp, "sandbox", "demo", "DEFAULT", LOCAL_URL);
            assertTrue(Files.readString(tmp.resolve("global-config.xml")).contains("<junit enabled=\"true\"/>"));
        }

        @Test
        void refusesUnsafeNamesAndUrls() {
            FolderScaffolder sc = new FolderScaffolder();
            assertThrows(ToolException.class, () -> sc.scaffold(tmp, "a b", "p", "DEFAULT", "http://x"));
            assertThrows(ToolException.class, () -> sc.scaffold(tmp, "e", "p", "DEFAULT", "ftp://x"));
        }
    }

    @Nested
    class Registry {

        private final ToolRegistry tools = TestSupport.registry("");

        private McpTool tool(final String name) {
            return tools.all().stream().filter(x -> x.name().equals(name)).findFirst().orElseThrow();
        }

        @Test
        void offersSevenTools() {
            assertEquals(List.of("testlum_lookup", "testlum_scaffold", "testlum_check", "testlum_run",
                    "testlum_write_test", "testlum_list_tests", "testlum_delete_test"),
                    tools.all().stream().map(McpTool::name).toList());
        }

        @Test
        void refusesAnArgumentTheToolDoesNotTake() {
            JsonNode args = JSON.createObjectNode().put("dir", tmp.toString()).put("path", "/work");
            String msg = assertThrows(ToolException.class, () -> tool("testlum_check").handler().apply(args))
                    .getMessage();
            assertTrue(msg.contains("Unknown argument 'path' for testlum_check") && msg.contains("dir"), msg);
        }

        @Test
        void refusesAMissingRequiredArgumentAndAFolderThatDoesNotExist() {
            JsonNode noName = JSON.createObjectNode().put("dir", tmp.toString());
            assertTrue(assertThrows(ToolException.class, () -> tool("testlum_delete_test").handler().apply(noName))
                    .getMessage().contains("Missing required argument: name"));
            JsonNode elsewhere = JSON.createObjectNode().put("dir", tmp.resolve("nope").toString());
            assertTrue(assertThrows(ToolException.class, () -> tool("testlum_list_tests").handler().apply(elsewhere))
                    .getMessage().contains("Not a folder"));
        }

        @Test
        void runNeedsTheEngineJar() {
            JsonNode args = JSON.createObjectNode().put("dir", tmp.toString());
            assertTrue(assertThrows(ToolException.class, () -> tool("testlum_run").handler().apply(args))
                    .getMessage().contains("TESTLUM_ENGINE_JAR"));
        }

        @Test
        void aToolRunsThroughTheRegistry() {
            JsonNode args = JSON.createObjectNode().put("path", "http/get");
            assertTrue(tool("testlum_lookup").handler().apply(args).contains("endpoint"));
        }

        @Test
        void everyToolDoesItsJobThroughTheRegistry() {
            String dir = tmp.toString();
            assertTrue(tool("testlum_scaffold").handler().apply(JSON.createObjectNode().put("dir", dir))
                    .contains("created global-config.xml"));
            assertTrue(tool("testlum_check").handler().apply(JSON.createObjectNode().put("dir", dir))
                    .startsWith("PASS"));
            var write = JSON.createObjectNode().put("dir", dir).put("name", "ping").put("scenario", SCENARIO);
            assertTrue(tool("testlum_write_test").handler().apply(write).contains("ping/scenario.xml"));
            String listed = tool("testlum_list_tests").handler().apply(JSON.createObjectNode().put("dir", dir));
            assertTrue(listed.contains("ping") && listed.contains("GET /ping"), listed);
            var delete = JSON.createObjectNode().put("dir", dir).put("name", "ping");
            assertTrue(tool("testlum_delete_test").handler().apply(delete).contains("ping"));
            assertFalse(Files.exists(tmp.resolve("scenarios").resolve("ping")));
        }
    }
}

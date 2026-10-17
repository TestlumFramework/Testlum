package com.testlum.testing.framework.mcp;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.testlum.testing.framework.mcp.exception.ToolException;
import com.testlum.testing.framework.mcp.constant.ToolMessage;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/** The tools: look up commands, scaffold, check, run, write a test, list and delete tests. */
@Component
@RequiredArgsConstructor
public final class ToolRegistry {

    private static final int DEFAULT_TIMEOUT_SECONDS = 600;
    private static final String DIR_HELP = "Tests folder. Leave out for /work. " + WorkDirectoryResolver.RULE;
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static final String LOOKUP_DOC =
            "Describe what the Testlum engine accepts, straight from its XSD schemas. With no path it lists "
                    + "the commands allowed in a scenario. With a path such as http or http/get it lists that "
                    + "element's attributes (required or optional, allowed values, patterns) and child elements. "
                    + "Use it instead of the wiki, which is partly stale.";

    private static final String SCAFFOLD_DOC =
            "Create the folder layout the engine demands (global-config.xml, config/<env>/integration.xml, "
                    + "scenarios/, data/, locators/pages, locators/component) with valid placeholders. Never "
                    + "overwrites an existing file.";

    private static final String CHECK_DOC =
            "Validate a tests folder against the engine's XSDs without running it: global-config.xml, each "
                    + "integration.xml and each scenario.xml. Returns PASS or FAIL then one line per finding as "
                    + "file:line:column [level] message. Everything beyond syntax (files, aliases, includes) is "
                    + "checked by the engine itself, so use testlum_run for the final word.";

    private static final String LIST_TESTS_DOC =
            "List the scenarios in a tests folder: name, tags (ticket ids go here), active or not, the "
                    + "variations file, the description, every http call as VERB endpoint and every include. "
                    + "Use it to match tests to tickets and to spot calls that no longer exist.";

    private static final String DELETE_TEST_DOC =
            "Delete scenarios/<name> and the scenarios under it. Refuses while a scenario outside them "
                    + "includes one, or while any scenario cannot be read. Never deletes data files: it names the ones "
                    + "no scenario uses any more and leaves them in place.";

    private static final String RUN_DOC =
            "Run the Testlum engine headless on a tests folder (java -jar, --config and --path) and report the "
                    + "exit code with its meaning, the scenario counts and a pass or fail line per run read from "
                    + "report/junit.xml, and the full failure text. Exit zero counts as passed only when the "
                    + "report shows runs and none failed. Needs TESTLUM_ENGINE_JAR.";

    private static final String WRITE_TEST_DOC =
            "Write scenarios/<name>/scenario.xml (name may be a path such as group/login) plus its data files in "
                    + "one step. files maps a file name to its content: request_1.json and expected_1.json land "
                    + "next to scenario.xml, and data/<name>.csv lands in the data folder, which is where a "
                    + "<settings><variations> file is read from, so one call writes a scenario that runs once "
                    + "per csv row. The scenario must pass the engine schema or nothing is written. Refuses to "
                    + "replace an existing scenario or csv unless overwrite is set, and never "
                    + "a csv another scenario reads.";

    private final EngineSchemas schemas;
    private final FolderScaffolder scaffolder;
    private final EngineRunner runner;
    private final ScenarioWriter writer;
    private final WorkDirectoryResolver dirs;
    private final ScenarioCatalog tests;

    public List<McpTool> all() {
        return List.of(lookup(), scaffold(), check(), run(), writeTest(), listTests(), deleteTest());
    }

    private McpTool lookup() {
        return tool("testlum_lookup", LOOKUP_DOC,
                schema(props()
                        .add("kind", prop("string", "scenario (default), global-config or integration"))
                        .add("path", prop("string", "Slash path under the root, e.g. http/get or assert/equal"))),
                a -> schemas.describe(text(a, "kind", "scenario"), text(a, "path", "")));
    }

    private McpTool scaffold() {
        return tool("testlum_scaffold", SCAFFOLD_DOC,
                schema(props()
                        .add("dir", prop("string", "Tests folder to create or complete. Leave out for /work. "
                                + WorkDirectoryResolver.RULE))
                        .add("env", prop("string", "Environment folder name, default sandbox"))
                        .add("project", prop("string", "Report project name, default tests"))
                        .add("apiAlias", prop("string", "API alias for integration.xml, default DEFAULT"))
                        .add("apiUrl", prop("string", "API base url, default http://localhost:8080"))),
                a -> scaffolder.scaffold(dirs.resolve(a), text(a, "env", "sandbox"), text(a, "project", "tests"),
                        text(a, "apiAlias", "DEFAULT"), text(a, "apiUrl", "http://localhost:8080")));
    }

    private McpTool check() {
        return tool("testlum_check", CHECK_DOC,
                schema(props()
                        .add("dir", prop("string", DIR_HELP))
                        .add("scenario", prop("string", "Only check this scenario folder name (default all)"))
                        .add("config", prop("string", "Global config file name, default " + Layout.GLOBAL_CONFIG))),
                a -> schemas.checkFolder(dirs.existing(a), text(a, "scenario", ""),
                        text(a, "config", Layout.GLOBAL_CONFIG)));
    }

    private McpTool run() {
        return tool("testlum_run", RUN_DOC,
                schema(props()
                        .add("dir", prop("string", DIR_HELP))
                        .add("tags", prop("string", "Comma separated tags: run only scenarios with these tags"))
                        .add("config", prop("string", "Global config file name, default " + Layout.GLOBAL_CONFIG))
                        .add("timeoutSeconds", prop("integer", "Kill the engine after this long, default "
                                + DEFAULT_TIMEOUT_SECONDS))),
                a -> runner.run(new EngineRunner.Job(dirs.existing(a), text(a, "config", Layout.GLOBAL_CONFIG),
                        text(a, "tags", ""), a.path("timeoutSeconds").asInt(DEFAULT_TIMEOUT_SECONDS))));
    }

    private McpTool writeTest() {
        return tool("testlum_write_test", WRITE_TEST_DOC,
                schema(props()
                        .add("dir", prop("string", DIR_HELP))
                        .add("name", prop("string", "Scenario folder name, or a path under scenarios/"))
                        .add("scenario", prop("string", "The full scenario.xml text"))
                        .add("files", prop("object", "File name to content, for example expected_1.json, or "
                                + "data/users.csv for a variations file"))
                        .add("overwrite", prop("boolean", "Replace existing files of this scenario")),
                        "name", "scenario"),
                a -> writer.writeTest(dirs.existing(a), required(a, "name"), required(a, "scenario"),
                        a.get("files"), a.path("overwrite").asBoolean(false)));
    }

    private McpTool listTests() {
        return tool("testlum_list_tests", LIST_TESTS_DOC,
                schema(props().add("dir", prop("string", DIR_HELP))),
                a -> tests.list(dirs.existing(a)));
    }

    private McpTool deleteTest() {
        return tool("testlum_delete_test", DELETE_TEST_DOC,
                schema(props()
                        .add("dir", prop("string", DIR_HELP))
                        .add("name", prop("string", "Scenario folder name")), "name"),
                a -> tests.delete(dirs.existing(a), required(a, "name")));
    }

    // ---- small schema helpers ----

    /** Fluent holder for the properties of an input schema. */
    private static final class Props {
        private final ObjectNode node = JSON.createObjectNode();

        Props add(final String name, final ObjectNode def) {
            node.set(name, def);
            return this;
        }
    }

    /** Builds a tool whose handler first refuses any argument the input schema does not list. */
    private static McpTool tool(final String name, final String doc, final ObjectNode schema,
                                final Function<JsonNode, String> handler) {
        return new McpTool(name, doc, schema, a -> {
            Set<String> allowed = new LinkedHashSet<>(schema.path("properties").propertyNames());
            a.propertyNames().stream().filter(k -> !allowed.contains(k)).findFirst().ifPresent(k -> {
                throw new ToolException(ToolMessage.UNKNOWN_ARGUMENT, k, name, String.join(", ", allowed));
            });
            return handler.apply(a);
        });
    }

    private static Props props() {
        return new Props();
    }

    private static ObjectNode prop(final String type, final String description) {
        ObjectNode n = JSON.createObjectNode();
        n.put("type", type);
        n.put("description", description);
        return n;
    }

    private static ObjectNode schema(final Props properties, final String... required) {
        ObjectNode s = JSON.createObjectNode();
        s.put("type", "object");
        s.set("properties", properties.node);
        var req = s.putArray("required");
        for (String r : required) {
            req.add(r);
        }
        s.put("additionalProperties", false);
        return s;
    }

    static String text(final JsonNode a, final String key, final String fallback) {
        String v = a.path(key).asText("");
        return v.isBlank() ? fallback : v.trim();
    }

    static String required(final JsonNode a, final String key) {
        String v = text(a, key, "");
        if (v.isEmpty()) {
            throw new ToolException(ToolMessage.MISSING_ARGUMENT, key);
        }
        return v;
    }
}

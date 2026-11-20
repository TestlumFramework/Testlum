package com.testlum.testing.framework.mcp.constant;

/** Texts of the errors the tools report back to the client. */
public final class ToolMessage {

    public static final String CANNOT_LIST_SCENARIOS = "Cannot list scenarios: %s";
    public static final String NOT_A_FOLDER = "Not a folder: %s. %s";
    public static final String HOST_PATH = "'%s' is a path on the host, outside the mounted folder. %s";
    public static final String NO_CONFIG = "No %s in %s. Run testlum_scaffold first.";
    public static final String ENGINE_JAR_NOT_SET =
            "Set TESTLUM_ENGINE_JAR to the engine jar (a build with the JUnit report).";
    public static final String RUN_FAILED = "Run failed: %s";
    public static final String CANNOT_WRITE = "Cannot write %s: %s";
    public static final String BAD_API_URL = "apiUrl must start with http:// or https://";
    public static final String BAD_ENV_OR_ALIAS = "env and apiAlias may hold letters, digits, _ and - only";
    public static final String NO_SUCH_ELEMENT = "No element '%s' under %s. Its definition:\n%s";
    public static final String NO_CHILD_ELEMENTS = "%s has no child elements";
    public static final String NO_ROOT_ELEMENT = "Root element not found in schemas: %s";
    public static final String BAD_KIND = "kind must be scenario, global-config or integration";
    public static final String CANNOT_INDEX_SCHEMAS = "Cannot index schemas: %s";
    public static final String CANNOT_COMPILE_SCHEMA = "Cannot compile %s: %s";
    public static final String NO_SCHEMAS =
            "No schemas available. Set TESTLUM_XSD_DIR to the folder holding the engine's "
                    + "schema/*.xsd files (engine/src/main/resources/schema in the repository).";
    public static final String XSD_DIR_WITHOUT_SCENARIO = "TESTLUM_XSD_DIR has no scenario.xsd: %s";
    public static final String NO_SCENARIOS_FOLDER = "No scenarios folder under %s";
    public static final String STILL_INCLUDED = "Not deleted: scenario %s includes %s";
    public static final String CANNOT_VERIFY_INCLUDES = "Not deleted: %s may be included by a scenario that cannot "
            + "be read (%s), fix or remove those first";
    public static final String NO_SUCH_SCENARIO = "No such scenario: %s";
    public static final String CANNOT_DELETE = "Could not delete: %s";
    public static final String BAD_SCENARIO_NAME =
            "Scenario name is its path under scenarios/: letters, digits, - and _, with / between folders";
    public static final String MISSING_ARGUMENT = "Missing required argument: %s";
    public static final String UNKNOWN_ARGUMENT =
            "Unknown argument '%s' for %s. Allowed: %s";
    public static final String BAD_FILE_NAME =
            "File name '%s' not allowed: use a plain name, or data/<name>.csv for a variations file";
    public static final String CANNOT_SHOW_SCHEMA = "Cannot show the schema: %s";
    public static final String NOT_VALID = "Not written, the scenario does not pass the schema (root must be "
            + "<scenario xmlns=\"%s\">):\n%s";
    public static final String CANNOT_STAGE = "Cannot stage the scenario: %s";
    public static final String UNCHECKABLE_INCLUDE = "Not deleted: scenario %s includes %s, which has a variable "
            + "and a relative part, so it could be any scenario";
    public static final String TEMP_NOT_REMOVED =
            "Not written: the temporary file used to check the scenario could not be removed: %s";
    public static final String CANNOT_CLEAN_UP = "Written, but these leftovers could not be removed: %s";
    public static final String CSV_UNVERIFIED = "Not written: data/%s may be read by a scenario that cannot be read "
            + "(%s), fix or remove those first";
    public static final String CSV_IN_USE =
            "data/%s is read by %s. Write this scenario with a csv of its own name instead.";
    public static final String ALREADY_EXISTS =
            "%s already exists. Pass overwrite=true to replace it.";

    private ToolMessage() {
    }
}

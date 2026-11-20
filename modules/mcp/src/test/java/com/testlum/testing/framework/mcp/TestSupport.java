package com.testlum.testing.framework.mcp;

import java.nio.file.Path;

final class TestSupport {

    private TestSupport() {
    }

    /** The engine's own XSDs in this repository, so the tests never need a built engine. */
    static EngineSchemas schemas() {
        Path dir = Path.of("..", "..", "engine", "src", "main", "resources", "schema").toAbsolutePath().normalize();
        return new EngineSchemas(dir.toString());
    }

    static ScenarioWriter writer() {
        return new ScenarioWriter(schemas(), new ScenarioCatalog());
    }

    static ToolRegistry registry(final String engineJar) {
        return new ToolRegistry(schemas(), new FolderScaffolder(), new EngineRunner(engineJar), writer(),
                new WorkDirectoryResolver("", ""), new ScenarioCatalog());
    }
}

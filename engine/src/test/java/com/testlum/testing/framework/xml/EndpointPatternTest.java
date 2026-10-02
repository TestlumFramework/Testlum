package com.testlum.testing.framework.xml;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class EndpointPatternTest {

    @TempDir
    Path tempDir;

    @Test
    void acceptsEndpointContainingOnlyVariable() throws IOException {
        String scenario = """
                <?xml version="1.0" encoding="UTF-8"?>
                <scenario xmlns="http://www.testlum.com/testing/model/scenario">
                    <overview>
                        <name>Endpoint variable test</name>
                        <description>Verify endpoint variable schema validation</description>
                    </overview>
                    <settings/>
                    <http comment="Call resource endpoint">
                        <get endpoint="{{resourceEndpoint}}">
                            <response code="200"/>
                        </get>
                    </http>
                </scenario>
                """;

        Path scenarioFile = tempDir.resolve("scenario.xml");
        Files.writeString(scenarioFile, scenario);

        XMLParsers parsers = new XMLParsers();

        assertDoesNotThrow(() ->
                parsers.forScenario().process(scenarioFile.toFile()));
    }
}

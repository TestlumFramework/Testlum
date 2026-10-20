package com.testlum.testing.framework.xml;

import com.testlum.testing.model.scenario.Scenario;
import com.testlum.testing.model.scenario.Scroll;
import com.testlum.testing.model.scenario.Wait;
import com.testlum.testing.model.scenario.Web;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScenarioSchemaValidationTest {

    private static final String COMMENT = "valid comment with min 10 chars";

    private XMLParser<Scenario> parser;

    @BeforeEach
    void setUp() {
        XMLParsers xmlParsers = new XMLParsers();
        parser = xmlParsers.forScenario();
    }

    private File createScenarioFile(final Path tempDir, final String commandsXml) throws IOException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<scenario xmlns=\"http://www.testlum.com/testing/model/scenario\">\n"
                + "    <overview>\n"
                + "        <name>Test Scenario</name>\n"
                + "        <description>Test scenario description min 10 chars</description>\n"
                + "    </overview>\n"
                + "    <settings/>\n"
                + commandsXml + "\n"
                + "</scenario>";
        Path file = tempDir.resolve("scenario.xml");
        Files.writeString(file, xml);
        return file.toFile();
    }

    @Test
    void acceptsScrollWithVariableReference(@TempDir final Path tempDir) throws IOException {
        File file = createScenarioFile(tempDir,
                "    <web comment=\"" + COMMENT + "\">\n"
                + "        <scroll comment=\"" + COMMENT + "\" type=\"page\" value=\"{{scroll_amount}}\"/>\n"
                + "    </web>");
        Scenario scenario = parser.process(file);
        assertNotNull(scenario);
        Web web = (Web) scenario.getCommands().get(0);
        Scroll scroll = (Scroll) web.getClickOrInputOrAssert().get(0);
        assertEquals("{{scroll_amount}}", scroll.getValue());
    }

    @Test
    void acceptsScrollWithDottedVariableReference(@TempDir final Path tempDir) throws IOException {
        File file = createScenarioFile(tempDir,
                "    <web comment=\"" + COMMENT + "\">\n"
                + "        <scroll comment=\"" + COMMENT + "\" type=\"page\" value=\"{{vars.scroll_value}}\"/>\n"
                + "    </web>");
        Scenario scenario = parser.process(file);
        assertNotNull(scenario);
        Web web = (Web) scenario.getCommands().get(0);
        Scroll scroll = (Scroll) web.getClickOrInputOrAssert().get(0);
        assertEquals("{{vars.scroll_value}}", scroll.getValue());
    }

    @Test
    void acceptsScrollWithNumericValues(@TempDir final Path tempDir) throws IOException {
        File file = createScenarioFile(tempDir,
                "    <web comment=\"" + COMMENT + "\">\n"
                + "        <scroll comment=\"" + COMMENT + "\" type=\"page\" value=\"500\"/>\n"
                + "        <scroll comment=\"" + COMMENT + "\" type=\"page\" value=\"-200\"/>\n"
                + "    </web>");
        Scenario scenario = parser.process(file);
        assertNotNull(scenario);
        Web web = (Web) scenario.getCommands().get(0);
        Scroll scroll1 = (Scroll) web.getClickOrInputOrAssert().get(0);
        Scroll scroll2 = (Scroll) web.getClickOrInputOrAssert().get(1);
        assertEquals("500", scroll1.getValue());
        assertEquals("-200", scroll2.getValue());
    }

    @Test
    void acceptsWaitWithSpecialCharacterVariables(@TempDir final Path tempDir) throws IOException {
        File file = createScenarioFile(tempDir,
                "    <wait comment=\"" + COMMENT + "\" time=\"{{wait.timeout}}\"/>\n"
                + "    <wait comment=\"" + COMMENT + "\" time=\"{{wait-sec}}\"/>\n"
                + "    <wait comment=\"" + COMMENT + "\" time=\"{{ wait_val }}\"/>\n"
                + "    <wait comment=\"" + COMMENT + "\" time=\"10\"/>");
        Scenario scenario = parser.process(file);
        assertNotNull(scenario);
        assertEquals(4, scenario.getCommands().size());
        assertEquals("{{wait.timeout}}", ((Wait) scenario.getCommands().get(0)).getTime());
        assertEquals("{{wait-sec}}", ((Wait) scenario.getCommands().get(1)).getTime());
        assertEquals("{{ wait_val }}", ((Wait) scenario.getCommands().get(2)).getTime());
        assertEquals("10", ((Wait) scenario.getCommands().get(3)).getTime());
    }

    @Test
    void rejectsInvalidScrollValue(@TempDir final Path tempDir) throws IOException {
        File file = createScenarioFile(tempDir,
                "    <web comment=\"" + COMMENT + "\">\n"
                + "        <scroll comment=\"" + COMMENT + "\" type=\"page\" value=\"invalid_string\"/>\n"
                + "    </web>");
        assertThrows(RuntimeException.class, () -> parser.process(file));
    }

    @Test
    void rejectsInvalidWaitTime(@TempDir final Path tempDir) throws IOException {
        File file = createScenarioFile(tempDir,
                "    <wait comment=\"" + COMMENT + "\" time=\"invalid_time\"/>");
        assertThrows(RuntimeException.class, () -> parser.process(file));
    }
}

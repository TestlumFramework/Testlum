package com.testlum.testing.report.server;

import com.testlum.reporting.sdk.model.scenario.CommandReport;
import com.testlum.reporting.sdk.model.scenario.ScenarioReport;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.model.scenario.Overview;
import com.testlum.testing.report.ScenarioResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ScenarioReportMapper} verifying the mapping onto the reporting-sdk wire models.
 */
class ScenarioReportMapperTest {

    private final ScenarioReportMapper mapper = new ScenarioReportMapper();

    private static ScenarioResult scenario() {
        Overview overview = new Overview();
        overview.setName("Login");
        overview.setJira("JIRA-1");
        ScenarioResult result = new ScenarioResult();
        result.setId(7);
        result.setPath("login.xml");
        result.setBrowser("chrome");
        result.setEnvironment("staging");
        result.setExecutionTime(1500);
        result.setStartedAt(1_000L);
        result.setVariation(Map.of("user", "admin"));
        result.setOverview(overview);
        result.setCommands(List.of(container()));
        return result;
    }

    private static CommandResult container() {
        CommandResult click = new CommandResult();
        click.setCommandKey("Click");
        click.setBase64Screenshot("aGVsbG8=");
        click.setException(new IllegalStateException("not found"));
        CommandResult container = new CommandResult();
        container.setCommandKey("Repeat");
        container.setSuccess(true);
        container.put("Times", 2);
        container.put("Values", List.of("a", 1));
        container.put("Object", new StringBuilder("[x]"));
        container.setSubCommandsResult(List.of(click));
        return container;
    }

    @Test
    void mapsScenarioFields() {
        ScenarioReport report = mapper.map(scenario());

        assertEquals(7, report.getId());
        assertEquals("login.xml", report.getPath());
        assertEquals("chrome", report.getBrowser());
        assertEquals("staging", report.getEnvironment());
        assertEquals(1500, report.getExecutionTime());
        assertFalse(report.isSuccess());
        assertEquals("Login", report.getOverview().getName());
        assertEquals("JIRA-1", report.getOverview().getJira());
        assertEquals(1_000L, report.getStartedAt());
        assertEquals(Map.of("user", "admin"), report.getVariation());
        assertFalse(report.isSkipped());
    }

    @Test
    void mapsNestedCommandsWithErrorAndScreenshot() {
        CommandReport container = mapper.map(scenario()).getCommands().get(0);
        CommandReport click = container.getSubCommands().get(0);

        assertEquals("Repeat", container.getCommandKey());
        assertNull(container.getError());
        assertEquals("Click", click.getCommandKey());
        assertEquals("aGVsbG8=", click.getBase64Screenshot());
        assertEquals(IllegalStateException.class.getName(), click.getError().getType());
        assertEquals("not found", click.getError().getMessage());
    }

    @Test
    void keepsJsonValuesAndStringifiesOtherMetadata() {
        CommandReport container = mapper.map(scenario()).getCommands().get(0);

        assertEquals(2, container.getMetadata().get("Times"));
        assertEquals(List.of("a", "1"), container.getMetadata().get("Values"));
        assertEquals("[x]", container.getMetadata().get("Object"));
    }

    @Test
    void omitsEmptyMetadata() {
        CommandReport click = mapper.map(scenario()).getCommands().get(0).getSubCommands().get(0);

        assertNull(click.getMetadata());
        assertTrue(click.getSubCommands() == null || click.getSubCommands().isEmpty());
    }
}

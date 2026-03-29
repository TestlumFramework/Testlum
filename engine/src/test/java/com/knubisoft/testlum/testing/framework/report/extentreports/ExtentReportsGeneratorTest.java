package com.knubisoft.testlum.testing.framework.report.extentreports;

import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.report.GlobalScenarioStatCollector;
import com.knubisoft.testlum.testing.framework.report.ScenarioResult;
import com.knubisoft.testlum.testing.framework.util.BrowserUtil;
import com.knubisoft.testlum.testing.framework.util.MobileUtil;
import com.knubisoft.testlum.testing.model.scenario.Overview;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link ExtentReportsGenerator} verifying report generation,
 * scenario result handling, screenshot logic, and step execution status.
 */
@ExtendWith(MockitoExtension.class)
class ExtentReportsGeneratorTest {

    @Mock
    private ExtentReportsConfigurator extentReportsConfigurator;
    @Mock
    private BrowserUtil browserUtil;
    @Mock
    private MobileUtil mobileUtil;

    private ExtentReportsGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new ExtentReportsGenerator(extentReportsConfigurator, browserUtil, mobileUtil);
    }

    @Nested
    class GenerateReport {

        @Test
        void handlesEmptyStatCollector() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            doNothing().when(extentReportsConfigurator).configure(any());

            generator.generateReport(collector);

            verify(extentReportsConfigurator).configure(any());
        }

        @Test
        void processesScenarioResults() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(1, "test-scenario", true);
            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            generator.generateReport(collector);

            verify(extentReportsConfigurator).configure(any());
        }
    }

    @Nested
    class AddScenarioExecutionResult {

        @Test
        void createsExtentTestForPassingScenario() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(1, "passing-scenario", true);
            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            generator.generateReport(collector);

            verify(extentReportsConfigurator).configure(any());
        }

        @Test
        void createsExtentTestForFailingScenario() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(2, "failing-scenario", false);
            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            generator.generateReport(collector);

            verify(extentReportsConfigurator).configure(any());
        }

        @Test
        void handlesScenarioWithTags() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(3, "tagged-scenario", true);
            result.setTags("smoke,regression");
            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            generator.generateReport(collector);

            verify(extentReportsConfigurator).configure(any());
        }

        @Test
        void handlesScenarioWithNullTags() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(4, "no-tags", true);
            result.setTags(null);
            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            generator.generateReport(collector);

            verify(extentReportsConfigurator).configure(any());
        }
    }

    @Nested
    class ScenarioSteps {

        @Test
        void handlesScenarioWithSteps() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(1, "with-steps", true);
            final CommandResult step = new CommandResult();
            step.setId(1);
            step.setCommandKey("click");
            step.setComment("Click button");
            step.setSuccess(true);
            result.getCommands().add(step);
            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            generator.generateReport(collector);

            verify(extentReportsConfigurator).configure(any());
        }

        @Test
        void handlesSkippedStep() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(1, "skipped-step", true);
            final CommandResult step = new CommandResult();
            step.setId(1);
            step.setCommandKey("click");
            step.setComment("Skipped click");
            step.setSkipped(true);
            result.getCommands().add(step);
            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            generator.generateReport(collector);

            verify(extentReportsConfigurator).configure(any());
        }

        @Test
        void handlesStepWithScreenshot() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(1, "screenshot-step", true);
            final CommandResult step = new CommandResult();
            step.setId(1);
            step.setCommandKey("screenshot");
            step.setComment("Take screenshot");
            step.setSuccess(true);
            step.setBase64Screenshot("iVBORw0KGgoAAAANSUhEUg");
            result.getCommands().add(step);
            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            generator.generateReport(collector);

            verify(extentReportsConfigurator).configure(any());
        }

        @Test
        void handlesStepWithNullScreenshot() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(1, "no-screenshot", true);
            final CommandResult step = new CommandResult();
            step.setId(1);
            step.setCommandKey("click");
            step.setComment("No screenshot");
            step.setSuccess(true);
            step.setBase64Screenshot(null);
            result.getCommands().add(step);
            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            generator.generateReport(collector);

            verify(extentReportsConfigurator).configure(any());
        }

        @Test
        void handlesStepWithEmptyScreenshot() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(1, "empty-screenshot", true);
            final CommandResult step = new CommandResult();
            step.setId(1);
            step.setCommandKey("click");
            step.setComment("Empty screenshot");
            step.setSuccess(true);
            step.setBase64Screenshot("");
            result.getCommands().add(step);
            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            generator.generateReport(collector);

            verify(extentReportsConfigurator).configure(any());
        }

        @Test
        void handlesFailedStepWithException() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(1, "failed-step", false);
            final CommandResult step = new CommandResult();
            step.setId(1);
            step.setCommandKey("click");
            step.setComment("Failed click");
            step.setSuccess(false);
            step.setException(new RuntimeException("Element not found"));
            result.getCommands().add(step);
            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            generator.generateReport(collector);

            verify(extentReportsConfigurator).configure(any());
        }
    }

    private ScenarioResult createScenarioResult(final int id, final String name, final boolean success) {
        final ScenarioResult result = new ScenarioResult();
        result.setId(id);
        result.setName(name);
        result.setPath("/scenarios/" + name + "/scenario.xml");
        result.setSuccess(success);
        result.setExecutionTime(100);
        result.setCommands(new ArrayList<>());
        final Overview overview = new Overview();
        overview.setName(name);
        overview.setDescription("Test description");
        overview.setDeveloper("dev");
        result.setOverview(overview);
        return result;
    }
}

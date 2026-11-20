package com.testlum.testing.framework.report;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.testlum.testing.framework.report.extentreports.ExtentReportsConfigurator;
import com.testlum.testing.framework.report.extentreports.ExtentReportsGenerator;
import com.testlum.testing.framework.util.BrowserUtil;
import com.testlum.testing.framework.util.MobileUtil;
import com.testlum.testing.model.scenario.Overview;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/** A scenario that threw before its first step must not cost the run its HTML report. */
class FailedOutsideStepReportsTest {

    private static final String MISSING = "Unable to find value for key <missing>. Available keys: [a]";

    @TempDir
    Path folder;

    @Test
    void theHtmlReportIsWrittenAndNamesTheScenarioThatFailedBeforeItsFirstStep() throws Exception {
        GlobalScenarioStatCollector collector = collectorWithOneFailedOutsideStepAndOnePassing();

        assertDoesNotThrow(() -> htmlGenerator().generateReport(collector));

        String html = Files.readString(folder.resolve("report.html"));
        assertTrue(html.contains("failed-scenario"), "scenario name missing from the HTML report");
        assertTrue(html.contains("Unable to find value for key"), "failure message missing from the HTML report");
        assertTrue(html.contains("passing-scenario"), "the passing scenario must still be reported");
    }

    @Test
    void theJunitAndHtmlGeneratorsBothAcceptTheSameResultList() throws Exception {
        GlobalScenarioStatCollector collector = collectorWithOneFailedOutsideStepAndOnePassing();
        List<ScenarioResult> results = collector.getResults();

        String xml = assertDoesNotThrow(() -> JUnitReportGenerator.toXml(results, Map.of(), Map.of()));
        assertDoesNotThrow(() -> htmlGenerator().generateReport(collector));

        assertTrue(xml.contains("failures=\"1\""), xml);
        assertTrue(Files.readString(folder.resolve("report.html")).contains("failed-scenario"));
    }

    private static GlobalScenarioStatCollector collectorWithOneFailedOutsideStepAndOnePassing() {
        GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
        collector.addResult(ScenarioResult.failedOutsideStep("/t/scenarios/failed-scenario/scenario.xml",
                new IllegalArgumentException(MISSING)));
        ScenarioResult passing = new ScenarioResult();
        passing.setId(1);
        passing.setName("passing-scenario");
        passing.setPath("/t/scenarios/passing-scenario/scenario.xml");
        passing.setSuccess(true);
        Overview overview = new Overview();
        overview.setName("passing-scenario");
        overview.setDescription("A scenario that passes");
        passing.setOverview(overview);
        collector.addResult(passing);
        return collector;
    }

    private ExtentReportsGenerator htmlGenerator() {
        ExtentReportsConfigurator configurator = mock(ExtentReportsConfigurator.class);
        File report = folder.resolve("report.html").toFile();
        doAnswer(call -> {
            call.<ExtentReports>getArgument(0).attachReporter(new ExtentSparkReporter(report));
            return null;
        }).when(configurator).configure(any());
        return new ExtentReportsGenerator(configurator, mock(BrowserUtil.class), mock(MobileUtil.class));
    }
}

package com.testlum.testing.framework.report;

import com.testlum.testing.framework.TestResourceSettings;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JUnitReportGeneratorTest {

    @Nested
    class Output {

        @TempDir
        File folder;

        private JUnitReportGenerator generator() {
            TestResourceSettings settings = mock(TestResourceSettings.class);
            when(settings.getTestResourcesFolder()).thenReturn(folder);
            return new JUnitReportGenerator(settings);
        }

        @Test
        void writesReportJunitXmlUnderTheTestResourcesFolder() throws Exception {
            generator().generateReport(new GlobalScenarioStatCollector());
            assertTrue(Files.readString(folder.toPath().resolve("report/junit.xml")).contains("<testsuite"));
        }

        @Test
        void failsLoudlyAndLeavesNoTempFileWhenTheReportCannotBeMovedIntoPlace() throws Exception {
            Path inTheWay = Files.createDirectories(folder.toPath().resolve("report/junit.xml/in-the-way"));
            JUnitReportGenerator generator = generator();

            assertThrows(DefaultFrameworkException.class,
                    () -> generator.generateReport(new GlobalScenarioStatCollector()));
            assertTrue(Files.isDirectory(inTheWay));
            try (var files = Files.list(folder.toPath().resolve("report"))) {
                assertEquals(1, files.count());
            }
        }

        @Test
        void removesTheReportOfAnEarlierRun() throws Exception {
            Path report = Files.createDirectories(folder.toPath().resolve("report"));
            Files.writeString(report.resolve("junit.xml"), "stale");
            generator().removeOldReport();
            assertFalse(Files.exists(report.resolve("junit.xml")));
        }

        @Test
        void failsLoudlyWhenTheOldReportCannotBeRemoved() throws Exception {
            Files.createDirectories(folder.toPath().resolve("report/junit.xml/in-the-way"));
            JUnitReportGenerator generator = generator();

            assertThrows(DefaultFrameworkException.class, generator::removeOldReport);
        }

        @Test
        void aMissingOldReportIsFine() {
            generator().removeOldReport();
        }
    }

    @Nested
    class Suite {

        @Test
        void numbersAScenarioThatRanMoreThanOnce() throws Exception {
            String path = "/t/scenarios/user/scenario.xml";
            Element suite = parse(JUnitReportGenerator.toXml(List.of(result(path, true, null, 0),
                    result(path, false, "boom", 0), result("/t/scenarios/other/scenario.xml", true, null, 0)),
                    Map.of(), Map.of()));
            NodeList cases = suite.getElementsByTagName("testcase");
            assertEquals("user #1", ((Element) cases.item(0)).getAttribute("name"));
            assertEquals("user #2", ((Element) cases.item(1)).getAttribute("name"));
            assertEquals("other", ((Element) cases.item(2)).getAttribute("name"));
        }

        @Test
        void writesOneSuiteWithPassFailAndSkippedCases() throws Exception {
            ScenarioResult pass = result("/t/scenarios/pass/scenario.xml", true, null, 1500);
            ScenarioResult fail = result("/t/scenarios/fail/scenario.xml", false,
                    "Equality <1,2> is not equal.\u0001", 20);
            ScenarioResult pass2 = result("/t/scenarios/pass2/scenario.xml", true, null, 0);
            Map<String, String> skipped = Map.of("/off/scenario.xml", "inactive");
            Map<String, String> invalid = Map.of("/bad/scenario.xml", "Unknown element <foo>\nat line 3");

            Element suite = parse(JUnitReportGenerator.toXml(List.of(pass, fail, pass2), skipped, invalid));

            assertEquals("testsuite", suite.getTagName());
            assertEquals("5", suite.getAttribute("tests"));
            assertEquals("1", suite.getAttribute("failures"));
            assertEquals("1", suite.getAttribute("errors"));
            assertEquals("1", suite.getAttribute("skipped"));
            assertEquals("1.520", suite.getAttribute("time"));
            NodeList cases = suite.getElementsByTagName("testcase");
            assertEquals("pass", ((Element) cases.item(0)).getAttribute("name"));
            assertEquals("/t/scenarios/pass/scenario.xml", ((Element) cases.item(0)).getAttribute("classname"));
            assertEquals("1.500", ((Element) cases.item(0)).getAttribute("time"));
            Element failure = (Element) ((Element) cases.item(1)).getElementsByTagName("failure").item(0);
            assertNotNull(failure);
            assertEquals("Equality <1,2> is not equal.", failure.getAttribute("message").strip());
            Element skip = (Element) ((Element) cases.item(3)).getElementsByTagName("skipped").item(0);
            assertEquals("inactive", skip.getAttribute("message"));
            assertEquals("off", ((Element) cases.item(3)).getAttribute("name"));
            Element error = (Element) ((Element) cases.item(4)).getElementsByTagName("error").item(0);
            assertEquals("Unknown element <foo>", error.getAttribute("message"));
            assertEquals("bad", ((Element) cases.item(4)).getAttribute("name"));
        }
    }

    @Nested
    class FailureDetail {

        private static CommandResult step(final long id, final String key, final String comment) {
            CommandResult c = new CommandResult();
            c.setId(id);
            c.setCommandKey(key);
            c.setComment(comment);
            return c;
        }

        private Element failureOf(final ScenarioResult r) throws Exception {
            Element suite = parse(JUnitReportGenerator.toXml(List.of(r), Map.of(), Map.of()));
            return (Element) suite.getElementsByTagName("failure").item(0);
        }

        @Test
        void anHttpMismatchNamesTheStatusesNotJustErrors() throws Exception {
            CommandResult http = step(7, "Http", "login");
            http.put(CommandResultHelper.EXPECTED_HTTP_STATUS, 200);
            http.put(CommandResultHelper.HTTP_STATUS, 500);
            DefaultFrameworkException ex = new DefaultFrameworkException(
                    List.of(" Http code should be [200] but was [500]"));
            http.setException(ex);
            ScenarioResult r = result("/t/scenarios/s1/scenario.xml", true, null, 1);
            failAsRunnerDoes(r, ex, http);

            Element failure = failureOf(r);
            String message = failure.getAttribute("message");
            assertFalse(message.startsWith("Errors"), message);
            assertEquals("step 7 Http: Http code should be [200] but was [500]", message);
            String text = failure.getTextContent();
            assertTrue(text.contains("step: 7"), text);
            assertTrue(text.contains("command: Http"), text);
            assertTrue(text.contains("comment: login"), text);
            assertTrue(text.contains("error-type: DefaultFrameworkException"), text);
            assertTrue(text.contains("http: expected status 200, actual 500"), text);
        }

        @Test
        void aWrapperNamesTheInnermostFailingStep() throws Exception {
            CommandResult inner = step(5, "AssertEqual", "check total");
            IllegalStateException ex = new IllegalStateException("Equality <1,2> is not equal.");
            inner.setException(ex);
            CommandResult passed = step(4, "Var", null);
            passed.setSuccess(true);
            CommandResult repeat = step(3, "Repeat", null);
            repeat.setSubCommandsResult(List.of(passed, inner));
            repeat.setException(ex);
            ScenarioResult r = result("/t/scenarios/s2/scenario.xml", true, null, 1);
            failAsRunnerDoes(r, ex, repeat);

            String text = failureOf(r).getTextContent();
            assertTrue(text.contains("command: AssertEqual"), text);
            assertTrue(text.contains("comment: check total"), text);
            assertTrue(text.contains("error-type: IllegalStateException"), text);
            assertTrue(text.contains("step: 3"), text);
            assertTrue(text.contains("inner step: 5"), text);
        }

        @Test
        void aScenarioThatFailedOutsideAStepIsAFailedTestcase() throws Exception {
            ScenarioResult r = ScenarioResult.failedOutsideStep("/t/scenarios/s3/scenario.xml",
                    new IllegalArgumentException("Unable to find value for key <missing>. Available keys: [a]"));

            Element suite = parse(JUnitReportGenerator.toXml(List.of(r), Map.of(), Map.of()));
            assertEquals("1", suite.getAttribute("failures"));
            Element failure = (Element) suite.getElementsByTagName("failure").item(0);
            assertEquals("before step 1: Unable to find value for key <missing>. Available keys: [a]",
                    failure.getAttribute("message"));
            assertTrue(failure.getTextContent().contains("error-type: IllegalArgumentException"));
        }

        @Test
        void aResultWithoutStepDetailKeepsTheCauseAsMessage() throws Exception {
            ScenarioResult r = result("/t/scenarios/s4/scenario.xml", false, "Errors:\n Http body differs", 1);
            assertEquals("Http body differs", failureOf(r).getAttribute("message"));
        }
    }

    // The three setters ScenarioRunner applies to the scenario result when a step fails.
    private static void failAsRunnerDoes(final ScenarioResult r, final Exception ex, final CommandResult top) {
        r.setSuccess(false);
        r.setCause(ex.getMessage());
        r.setFailedStep(FailedStep.of(top, ex));
    }

    private static ScenarioResult result(final String path, final boolean ok, final String cause, final long ms) {
        ScenarioResult r = new ScenarioResult();
        r.setPath(path);
        r.setSuccess(ok);
        r.setCause(cause);
        r.setExecutionTime(ms);
        return r;
    }

    private static Element parse(final String xml) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))).getDocumentElement();
    }
}

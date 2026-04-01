package com.knubisoft.testlum.testing.framework.report.extentreports;

import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.report.GlobalScenarioStatCollector;
import com.knubisoft.testlum.testing.framework.report.ScenarioResult;
import com.knubisoft.testlum.testing.framework.util.BrowserUtil;
import com.knubisoft.testlum.testing.framework.util.MobileUtil;
import com.knubisoft.testlum.testing.model.global_config.AbstractBrowser;
import com.knubisoft.testlum.testing.model.global_config.AppiumCapabilities;
import com.knubisoft.testlum.testing.model.global_config.AppiumNativeCapabilities;
import com.knubisoft.testlum.testing.model.global_config.MobilebrowserDevice;
import com.knubisoft.testlum.testing.model.global_config.NativeDevice;
import com.knubisoft.testlum.testing.model.global_config.Platform;
import com.knubisoft.testlum.testing.model.scenario.Overview;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ExtentReportsGenerator} verifying report generation,
 * scenario result handling, screenshot logic, step execution status,
 * browser/device info tables, and metadata rendering.
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

        @Test
        void processesMultipleScenarioResultsSorted() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            collector.addResult(createScenarioResult(3, "third", true));
            collector.addResult(createScenarioResult(1, "first", true));
            collector.addResult(createScenarioResult(2, "second", false));
            doNothing().when(extentReportsConfigurator).configure(any());

            assertDoesNotThrow(() -> generator.generateReport(collector));
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

        @Test
        void handlesScenarioWithSingleTag() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(5, "single-tag", true);
            result.setTags("smoke");
            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            assertDoesNotThrow(() -> generator.generateReport(collector));
        }
    }

    @Nested
    class BrowserInfo {

        @Test
        void addsBrowserInfoWhenBrowserIsPresent() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(1, "browser-test", true);
            result.setBrowser("chrome-alias");
            result.setEnvironment("dev");

            AbstractBrowser browser = mock(AbstractBrowser.class);
            when(browserUtil.getBrowserBy("dev", "chrome-alias")).thenReturn(Optional.of(browser));
            when(browserUtil.getBrowserType(browser)).thenReturn(BrowserUtil.BrowserType.LOCAL);
            when(browserUtil.getBrowserVersion(browser, BrowserUtil.BrowserType.LOCAL)).thenReturn("120.0");

            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            assertDoesNotThrow(() -> generator.generateReport(collector));
            verify(browserUtil).getBrowserBy("dev", "chrome-alias");
        }

        @Test
        void skipsBrowserInfoWhenBrowserIsBlank() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(1, "no-browser", true);
            result.setBrowser("");
            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            generator.generateReport(collector);

            verify(browserUtil, never()).getBrowserBy(any(), any());
        }

        @Test
        void skipsBrowserInfoWhenBrowserIsNull() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(1, "null-browser", true);
            result.setBrowser(null);
            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            generator.generateReport(collector);

            verify(browserUtil, never()).getBrowserBy(any(), any());
        }
    }

    @Nested
    class MobileDeviceInfo {

        @Test
        void addsMobilebrowserDeviceInfoWhenPresent() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(1, "mobile-test", true);
            result.setMobilebrowserDevice("pixel-alias");
            result.setEnvironment("dev");

            MobilebrowserDevice device = mock(MobilebrowserDevice.class);
            AppiumCapabilities caps = mock(AppiumCapabilities.class);
            when(device.getAppiumCapabilities()).thenReturn(caps);
            when(caps.getDeviceName()).thenReturn("Pixel 6");
            when(caps.getPlatformVersion()).thenReturn("13.0");
            when(device.getPlatformName()).thenReturn(Platform.ANDROID);
            when(mobileUtil.getMobileBrowserDeviceBy("dev", "pixel-alias")).thenReturn(Optional.of(device));

            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            assertDoesNotThrow(() -> generator.generateReport(collector));
            verify(mobileUtil).getMobileBrowserDeviceBy("dev", "pixel-alias");
        }

        @Test
        void addsNativeDeviceInfoWhenPresent() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(1, "native-test", true);
            result.setNativeDevice("iphone-alias");
            result.setEnvironment("dev");

            NativeDevice device = mock(NativeDevice.class);
            AppiumNativeCapabilities caps = mock(AppiumNativeCapabilities.class);
            when(device.getAppiumCapabilities()).thenReturn(caps);
            when(caps.getDeviceName()).thenReturn("iPhone 15");
            when(caps.getPlatformVersion()).thenReturn("17.0");
            when(device.getPlatformName()).thenReturn(Platform.IOS);
            when(mobileUtil.getNativeDeviceBy("dev", "iphone-alias")).thenReturn(Optional.of(device));

            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            assertDoesNotThrow(() -> generator.generateReport(collector));
            verify(mobileUtil).getNativeDeviceBy("dev", "iphone-alias");
        }

        @Test
        void skipsMobilebrowserDeviceInfoWhenBlank() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(1, "no-mobile", true);
            result.setMobilebrowserDevice("");
            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            generator.generateReport(collector);

            verify(mobileUtil, never()).getMobileBrowserDeviceBy(any(), any());
        }

        @Test
        void skipsNativeDeviceInfoWhenNull() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(1, "no-native", true);
            result.setNativeDevice(null);
            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            generator.generateReport(collector);

            verify(mobileUtil, never()).getNativeDeviceBy(any(), any());
        }
    }

    @Nested
    class ScenarioSteps {

        @Test
        void handlesScenarioWithSteps() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(1, "with-steps", true);
            final CommandResult step = createStep(1, "click", "Click button", true, false);
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
            final CommandResult step = createStep(1, "click", "Skipped click", false, true);
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
            final CommandResult step = createStep(1, "screenshot", "Take screenshot", true, false);
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
            final CommandResult step = createStep(1, "click", "No screenshot", true, false);
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
            final CommandResult step = createStep(1, "click", "Empty screenshot", true, false);
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
            final CommandResult step = createStep(1, "click", "Failed click", false, false);
            step.setException(new RuntimeException("Element not found"));
            result.getCommands().add(step);
            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            generator.generateReport(collector);

            verify(extentReportsConfigurator).configure(any());
        }

        @Test
        void handlesStepWithMetadata() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(1, "metadata-step", true);
            final CommandResult step = createStep(1, "api-call", "API request", true, false);
            step.put("URL", "http://example.com");
            step.put("Method", "POST");
            result.getCommands().add(step);
            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            assertDoesNotThrow(() -> generator.generateReport(collector));
        }

        @Test
        void handlesStepWithListMetadata() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(1, "list-metadata", true);
            final CommandResult step = createStep(1, "api-call", "API headers", true, false);
            step.put("Headers", List.of("Content-Type: application/json", "Accept: */*"));
            result.getCommands().add(step);
            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            assertDoesNotThrow(() -> generator.generateReport(collector));
        }

        @Test
        void handlesStepWithExpectedAndActual() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(1, "comparison-step", true);
            final CommandResult step = createStep(1, "assert", "Compare values", true, false);
            step.setExpected("{\"key\": \"value\"}");
            step.setActual("{\"key\": \"value\"}");
            result.getCommands().add(step);
            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            assertDoesNotThrow(() -> generator.generateReport(collector));
        }

        @Test
        void handlesStepWithBlankExpectedAndActual() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(1, "no-comparison", true);
            final CommandResult step = createStep(1, "click", "No comparison", true, false);
            step.setExpected("");
            step.setActual("");
            result.getCommands().add(step);
            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            assertDoesNotThrow(() -> generator.generateReport(collector));
        }

        @Test
        void handlesStepWithIdZero() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(1, "zero-id-step", true);
            final CommandResult step = createStep(0, "auth", "Auth step", true, false);
            result.getCommands().add(step);
            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            assertDoesNotThrow(() -> generator.generateReport(collector));
        }

        @Test
        void handlesStepWithSubCommands() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(1, "sub-commands", true);
            final CommandResult parent = createStep(1, "auth", "Auth parent", true, false);
            final CommandResult child = createStep(2, "api-call", "Login call", true, false);
            parent.setSubCommandsResult(List.of(child));
            result.getCommands().add(parent);
            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            assertDoesNotThrow(() -> generator.generateReport(collector));
        }

        @Test
        void handlesAllSkippedStepsInPassingScenario() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(1, "all-skipped", true);
            result.getCommands().add(createStep(1, "click", "Skipped 1", false, true));
            result.getCommands().add(createStep(2, "type", "Skipped 2", false, true));
            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            assertDoesNotThrow(() -> generator.generateReport(collector));
        }

        @Test
        void handlesMixedSkippedAndPassedSteps() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(1, "mixed-skip", true);
            result.getCommands().add(createStep(1, "click", "Passed", true, false));
            result.getCommands().add(createStep(2, "type", "Skipped", false, true));
            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            assertDoesNotThrow(() -> generator.generateReport(collector));
        }
    }

    @Nested
    class OverviewInfo {

        @Test
        void handlesOverviewWithDeveloper() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(1, "dev-scenario", true);
            result.getOverview().setDeveloper("John Doe");
            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            assertDoesNotThrow(() -> generator.generateReport(collector));
        }

        @Test
        void handlesOverviewWithBlankDeveloper() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(1, "no-dev", true);
            result.getOverview().setDeveloper("");
            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            assertDoesNotThrow(() -> generator.generateReport(collector));
        }

        @Test
        void handlesOverviewWithLink() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(1, "link-scenario", true);
            result.getOverview().setLink("https://example.com/doc");
            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            assertDoesNotThrow(() -> generator.generateReport(collector));
        }

        @Test
        void handlesOverviewWithBlankLink() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(1, "no-link", true);
            result.getOverview().setLink("");
            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            assertDoesNotThrow(() -> generator.generateReport(collector));
        }

        @Test
        void handlesOverviewWithJiraLink() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(1, "jira-scenario", true);
            result.getOverview().setJira("https://jira.example.com/PROJ-123");
            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            assertDoesNotThrow(() -> generator.generateReport(collector));
        }

        @Test
        void handlesOverviewWithBlankJira() {
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            final ScenarioResult result = createScenarioResult(1, "no-jira", true);
            result.getOverview().setJira("");
            collector.addResult(result);
            doNothing().when(extentReportsConfigurator).configure(any());

            assertDoesNotThrow(() -> generator.generateReport(collector));
        }
    }

    @Nested
    class EvaluateCategories {

        @Test
        void evaluateCategoriesReturnsEmptyArrayForNullTags() throws Exception {
            Method method = ExtentReportsGenerator.class.getDeclaredMethod("evaluateCategories", ScenarioResult.class);
            method.setAccessible(true);
            ScenarioResult result = new ScenarioResult();
            result.setTags(null);

            String[] categories = (String[]) method.invoke(generator, result);

            assertEquals(0, categories.length);
        }

        @Test
        void evaluateCategoriesSplitsCommaSeparatedTags() throws Exception {
            Method method = ExtentReportsGenerator.class.getDeclaredMethod("evaluateCategories", ScenarioResult.class);
            method.setAccessible(true);
            ScenarioResult result = new ScenarioResult();
            result.setTags("smoke,regression,api");

            String[] categories = (String[]) method.invoke(generator, result);

            assertEquals(3, categories.length);
            assertEquals("smoke", categories[0]);
            assertEquals("regression", categories[1]);
            assertEquals("api", categories[2]);
        }

        @Test
        void evaluateCategoriesHandlesSingleTag() throws Exception {
            Method method = ExtentReportsGenerator.class.getDeclaredMethod("evaluateCategories", ScenarioResult.class);
            method.setAccessible(true);
            ScenarioResult result = new ScenarioResult();
            result.setTags("smoke");

            String[] categories = (String[]) method.invoke(generator, result);

            assertEquals(1, categories.length);
            assertEquals("smoke", categories[0]);
        }
    }

    @Nested
    class GetValueAsStringFromMetaData {

        @Test
        void formatsListAsUnsortedList() throws Exception {
            Method method = ExtentReportsGenerator.class
                    .getDeclaredMethod("getValueAsStringFromMetaData", Object.class);
            method.setAccessible(true);

            List<String> items = List.of("item1", "item2");
            String result = (String) method.invoke(generator, items);

            assertTrue(result.contains("<ul>"));
            assertTrue(result.contains("<li>item1</li>"));
            assertTrue(result.contains("<li>item2</li>"));
            assertTrue(result.contains("</ul>"));
        }

        @Test
        void formatsStringAsPreformattedText() throws Exception {
            Method method = ExtentReportsGenerator.class
                    .getDeclaredMethod("getValueAsStringFromMetaData", Object.class);
            method.setAccessible(true);

            String result = (String) method.invoke(generator, "hello");

            assertTrue(result.contains("<pre>hello</pre>"));
        }
    }

    private CommandResult createStep(final int id, final String key, final String comment,
                                     final boolean success, final boolean skipped) {
        final CommandResult step = new CommandResult();
        step.setId(id);
        step.setCommandKey(key);
        step.setComment(comment);
        step.setSuccess(success);
        step.setSkipped(skipped);
        return step;
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

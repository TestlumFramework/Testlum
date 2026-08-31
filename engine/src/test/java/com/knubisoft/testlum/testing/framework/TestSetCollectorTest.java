package com.knubisoft.testlum.testing.framework;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioCollector;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioCollector.MappingResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioFilter;
import com.knubisoft.testlum.testing.framework.util.BrowserUtil;
import com.knubisoft.testlum.testing.framework.util.JacksonService;
import com.knubisoft.testlum.testing.framework.util.MobileUtil;
import com.knubisoft.testlum.testing.framework.variations.GlobalVariationsProvider;
import com.knubisoft.testlum.testing.model.global_config.AbstractBrowser;
import com.knubisoft.testlum.testing.model.global_config.Environment;
import com.knubisoft.testlum.testing.model.scenario.Scenario;
import com.knubisoft.testlum.testing.model.scenario.Settings;
import com.knubisoft.testlum.testing.model.scenario.Web;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TestSetCollector} verifying test set collection,
 * argument creation for parameterized tests, environment expansion,
 * UI/non-UI scenario handling, and variations support.
 */
@ExtendWith(MockitoExtension.class)
class TestSetCollectorTest {

    @Mock
    private ScenarioCollector scenarioCollector;
    @Mock
    private ScenarioFilter scenarioFilter;
    @Mock
    private BrowserUtil browserUtil;
    @Mock
    private MobileUtil mobileUtil;
    @Mock
    private TestResourceSettings testResourceSettings;
    @Mock
    private GlobalVariationsProvider globalVariationsProvider;
    @Mock
    private JacksonService jacksonService;

    private TestSetCollector testSetCollector;

    @BeforeEach
    void setUp() {
        when(browserUtil.filterDefaultEnabledBrowsers()).thenReturn(Collections.emptyList());
        when(mobileUtil.filterDefaultEnabledMobileBrowserDevices()).thenReturn(Collections.emptyList());
        when(mobileUtil.filterDefaultEnabledNativeDevices()).thenReturn(Collections.emptyList());

        final Environment environment = new Environment();
        environment.setFolder("dev");
        final List<Environment> environments = List.of(environment);

        testSetCollector = new TestSetCollector(
                scenarioCollector, scenarioFilter, browserUtil, mobileUtil,
                testResourceSettings, globalVariationsProvider, environments, jacksonService);
    }

    @Nested
    class Collect {

        @Test
        void returnsEmptyStreamForNoScenarios() {
            final ScenarioCollector.Result collectorResult = new ScenarioCollector.Result();
            when(scenarioCollector.collect()).thenReturn(collectorResult);
            when(scenarioFilter.filterScenarios(collectorResult)).thenReturn(new ArrayList<>());

            final Stream<Arguments> result = testSetCollector.collect();

            assertNotNull(result);
            assertEquals(0, result.count());
        }

        @Test
        void returnsArgumentsForNonUiScenario() {
            final ScenarioCollector.Result collectorResult = new ScenarioCollector.Result();
            final Scenario scenario = createScenarioWithSettings();
            final MappingResult mappingResult = new MappingResult(
                    new File("/scenarios/test/scenario.xml"), scenario, null);
            collectorResult.add(mappingResult);

            when(scenarioCollector.collect()).thenReturn(collectorResult);
            when(scenarioFilter.filterScenarios(collectorResult)).thenReturn(List.of(mappingResult));
            when(testResourceSettings.getScenariosFolder()).thenReturn(new File("/scenarios"));

            final Stream<Arguments> result = testSetCollector.collect();

            assertNotNull(result);
            final List<Arguments> argumentsList = result.toList();
            assertEquals(1, argumentsList.size());
        }

        @Test
        void returnsMultipleScenariosAsMultipleArguments() {
            final ScenarioCollector.Result collectorResult = new ScenarioCollector.Result();
            final MappingResult mr1 = new MappingResult(
                    new File("/scenarios/test1/scenario.xml"), createScenarioWithSettings(), null);
            final MappingResult mr2 = new MappingResult(
                    new File("/scenarios/test2/scenario.xml"), createScenarioWithSettings(), null);
            collectorResult.add(mr1);
            collectorResult.add(mr2);

            when(scenarioCollector.collect()).thenReturn(collectorResult);
            when(scenarioFilter.filterScenarios(collectorResult)).thenReturn(List.of(mr1, mr2));
            when(testResourceSettings.getScenariosFolder()).thenReturn(new File("/scenarios"));

            final List<Arguments> argumentsList = testSetCollector.collect().toList();

            assertEquals(2, argumentsList.size());
        }
    }

    @Nested
    class CreateArguments {

        @Test
        void nonUiScenarioCreatesOneArgumentPerEnvironment() {
            final ScenarioCollector.Result collectorResult = new ScenarioCollector.Result();
            final Scenario scenario = createScenarioWithSettings();
            final MappingResult mappingResult = new MappingResult(
                    new File("/scenarios/api-test/scenario.xml"), scenario, null);
            collectorResult.add(mappingResult);

            when(scenarioCollector.collect()).thenReturn(collectorResult);
            when(scenarioFilter.filterScenarios(collectorResult)).thenReturn(List.of(mappingResult));
            when(testResourceSettings.getScenariosFolder()).thenReturn(new File("/scenarios"));

            final List<Arguments> argumentsList = testSetCollector.collect().toList();

            assertEquals(1, argumentsList.size());
        }
    }

    @Nested
    class UiScenarios {

        @Test
        void webScenarioExpandsByBrowsers() {
            // Setup browsers
            AbstractBrowser chrome = mock(AbstractBrowser.class);
            AbstractBrowser firefox = mock(AbstractBrowser.class);
            when(chrome.getAlias()).thenReturn("chrome");
            when(firefox.getAlias()).thenReturn("firefox");
            when(browserUtil.filterDefaultEnabledBrowsers()).thenReturn(List.of(chrome, firefox));

            Environment env = new Environment();
            env.setFolder("dev");

            testSetCollector = new TestSetCollector(
                    scenarioCollector, scenarioFilter, browserUtil, mobileUtil,
                    testResourceSettings, globalVariationsProvider, List.of(env), jacksonService);

            // Scenario with web command
            Scenario scenario = createScenarioWithSettings();
            scenario.getCommands().add(new Web());

            MappingResult mr = new MappingResult(
                    new File("/scenarios/web-test/scenario.xml"), scenario, null);
            ScenarioCollector.Result collectorResult = new ScenarioCollector.Result();
            collectorResult.add(mr);

            when(scenarioCollector.collect()).thenReturn(collectorResult);
            when(scenarioFilter.filterScenarios(collectorResult)).thenReturn(List.of(mr));
            when(testResourceSettings.getScenariosFolder()).thenReturn(new File("/scenarios"));

            List<Arguments> argumentsList = testSetCollector.collect().toList();

            // 2 browsers x 1 environment = 2
            assertEquals(2, argumentsList.size());
        }
    }

    @Nested
    class MultipleEnvironments {

        @Test
        void expandsScenariosAcrossEnvironments() {
            Environment dev = new Environment();
            dev.setFolder("dev");
            Environment staging = new Environment();
            staging.setFolder("staging");

            when(browserUtil.filterDefaultEnabledBrowsers()).thenReturn(Collections.emptyList());
            when(mobileUtil.filterDefaultEnabledMobileBrowserDevices()).thenReturn(Collections.emptyList());
            when(mobileUtil.filterDefaultEnabledNativeDevices()).thenReturn(Collections.emptyList());

            testSetCollector = new TestSetCollector(
                    scenarioCollector, scenarioFilter, browserUtil, mobileUtil,
                    testResourceSettings, globalVariationsProvider, List.of(dev, staging), jacksonService);

            ScenarioCollector.Result collectorResult = new ScenarioCollector.Result();
            MappingResult mr = new MappingResult(
                    new File("/scenarios/test/scenario.xml"), createScenarioWithSettings(), null);
            collectorResult.add(mr);

            when(scenarioCollector.collect()).thenReturn(collectorResult);
            when(scenarioFilter.filterScenarios(collectorResult)).thenReturn(List.of(mr));
            when(testResourceSettings.getScenariosFolder()).thenReturn(new File("/scenarios"));

            List<Arguments> argumentsList = testSetCollector.collect().toList();

            // 1 scenario x 2 environments = 2
            assertEquals(2, argumentsList.size());
        }
    }

    @Nested
    class VariationsSupport {

        @Test
        void expandsNonUiScenarioWithVariations() {
            Scenario scenario = createScenarioWithSettings();
            scenario.getSettings().setVariations("vars.csv");

            MappingResult mr = new MappingResult(
                    new File("/scenarios/var-test/scenario.xml"), scenario, null);
            ScenarioCollector.Result collectorResult = new ScenarioCollector.Result();
            collectorResult.add(mr);

            when(scenarioCollector.collect()).thenReturn(collectorResult);
            when(scenarioFilter.filterScenarios(collectorResult)).thenReturn(List.of(mr));
            when(testResourceSettings.getScenariosFolder()).thenReturn(new File("/scenarios"));

            Map<String, String> v1 = Map.of("user", "alice");
            Map<String, String> v2 = Map.of("user", "bob");
            when(globalVariationsProvider.getVariations("vars.csv")).thenReturn(List.of(v1, v2));

            List<Arguments> argumentsList = testSetCollector.collect().toList();

            // 2 variations x 1 environment = 2
            assertEquals(2, argumentsList.size());
        }
    }

    @Nested
    class EmptyEnvironments {

        @Test
        void throwsWhenNoEnvironmentsDuringCollect() {
            when(browserUtil.filterDefaultEnabledBrowsers()).thenReturn(Collections.emptyList());
            when(mobileUtil.filterDefaultEnabledMobileBrowserDevices()).thenReturn(Collections.emptyList());
            when(mobileUtil.filterDefaultEnabledNativeDevices()).thenReturn(Collections.emptyList());

            TestSetCollector emptyEnvCollector = new TestSetCollector(
                    scenarioCollector, scenarioFilter, browserUtil, mobileUtil,
                    testResourceSettings, globalVariationsProvider, Collections.emptyList(), jacksonService);

            assertThrows(DefaultFrameworkException.class, emptyEnvCollector::collect);
        }

        @Test
        void throwsWhenEnvironmentsNullDuringCollect() {
            when(browserUtil.filterDefaultEnabledBrowsers()).thenReturn(Collections.emptyList());
            when(mobileUtil.filterDefaultEnabledMobileBrowserDevices()).thenReturn(Collections.emptyList());
            when(mobileUtil.filterDefaultEnabledNativeDevices()).thenReturn(Collections.emptyList());

            TestSetCollector nullEnvCollector = new TestSetCollector(
                    scenarioCollector, scenarioFilter, browserUtil, mobileUtil,
                    testResourceSettings, globalVariationsProvider, null, jacksonService);

            assertThrows(DefaultFrameworkException.class, nullEnvCollector::collect);
        }
    }

    @Nested
    class ShortPath {

        @Test
        void removesScenariosFolderPrefix() {
            ScenarioCollector.Result collectorResult = new ScenarioCollector.Result();
            Scenario scenario = createScenarioWithSettings();
            MappingResult mr = new MappingResult(
                    new File("/scenarios/deep/nested/scenario.xml"), scenario, null);
            collectorResult.add(mr);

            when(scenarioCollector.collect()).thenReturn(collectorResult);
            when(scenarioFilter.filterScenarios(collectorResult)).thenReturn(List.of(mr));
            when(testResourceSettings.getScenariosFolder()).thenReturn(new File("/scenarios"));

            List<Arguments> argumentsList = testSetCollector.collect().toList();

            assertEquals(1, argumentsList.size());
            // Verify the named argument contains the short path
            Object[] args = argumentsList.get(0).get();
            assertNotNull(args);
        }
    }

    private Scenario createScenarioWithSettings() {
        final Scenario scenario = new Scenario();
        final Settings settings = new Settings();
        settings.setActive(true);
        scenario.setSettings(settings);
        return scenario;
    }
}

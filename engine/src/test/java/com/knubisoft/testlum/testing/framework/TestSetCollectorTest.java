package com.knubisoft.testlum.testing.framework;

import com.knubisoft.testlum.testing.framework.scenario.ScenarioCollector;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioCollector.MappingResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioFilter;
import com.knubisoft.testlum.testing.framework.util.BrowserUtil;
import com.knubisoft.testlum.testing.framework.util.MobileUtil;
import com.knubisoft.testlum.testing.framework.variations.GlobalVariationsProvider;
import com.knubisoft.testlum.testing.model.global_config.Environment;
import com.knubisoft.testlum.testing.model.scenario.Scenario;
import com.knubisoft.testlum.testing.model.scenario.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TestSetCollector} verifying test set collection
 * and argument creation for parameterized tests.
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
                testResourceSettings, globalVariationsProvider, environments);
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
            final Scenario scenario = new Scenario();
            final Settings settings = new Settings();
            settings.setActive(true);
            scenario.setSettings(settings);
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
    }

    @Nested
    class CreateArguments {

        @Test
        void nonUiScenarioCreatesOneArgumentPerEnvironment() {
            final ScenarioCollector.Result collectorResult = new ScenarioCollector.Result();
            final Scenario scenario = new Scenario();
            final Settings settings = new Settings();
            settings.setActive(true);
            scenario.setSettings(settings);
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
}

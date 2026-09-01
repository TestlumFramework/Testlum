package com.testlum.testing.framework.scenario;

import com.testlum.testing.framework.constant.LogMessage;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.exception.IntegrationDisabledException;
import com.testlum.testing.framework.scenario.ScenarioCollector.MappingResult;
import com.testlum.testing.framework.util.LogUtil;
import com.testlum.testing.logger.ConfigurationLogger;
import com.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.testlum.testing.model.global_config.RunScenariosByTag;
import com.testlum.testing.model.global_config.TagValue;
import com.testlum.testing.model.scenario.Scenario;
import com.testlum.testing.model.scenario.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ScenarioFilterTest {

    private GlobalTestConfiguration config;
    private LogUtil logUtil;
    private ConfigurationLogger configurationLogger;
    private ScenarioFilter filter;

    @BeforeEach
    void setUp() {
        config = mock(GlobalTestConfiguration.class);
        logUtil = mock(LogUtil.class);
        doNothing().when(logUtil).logNonParsedScenarioInfo(anyString(), anyString());
        doNothing().when(logUtil).logScenarioWithoutTags(anyString());
        configurationLogger = mock(ConfigurationLogger.class);
        filter = new ScenarioFilter(config, logUtil, configurationLogger);
        ScenarioStatusRegistry.clear();

        final RunScenariosByTag runByTag = new RunScenariosByTag();
        runByTag.setEnabled(false);
        when(config.getRunScenariosByTag()).thenReturn(runByTag);
    }

    private MappingResult validResult(final String fileName, final boolean active,
                                      final boolean onlyThis, final String tags) {
        final Scenario scenario = new Scenario();
        final Settings settings = new Settings();
        settings.setActive(active);
        settings.setOnlyThis(onlyThis);
        settings.setTags(tags);
        scenario.setSettings(settings);
        return new MappingResult(new File(fileName), scenario, null);
    }

    private MappingResult nonParsedResult(final String fileName) {
        return new MappingResult(new File(fileName), null, new RuntimeException("parse error"));
    }

    private MappingResult invalidResult(final String fileName, final String tags) {
        return invalidResult(fileName, tags, new RuntimeException("validation failed"));
    }

    private MappingResult invalidResult(final String fileName, final String tags, final Exception exception) {
        final MappingResult parsed = validResult(fileName, true, false, tags);
        return new MappingResult(parsed.file, parsed.scenario, exception);
    }

    @Nested
    class FilterIsActive {
        @Test
        void removesInactiveScenarios() {
            final List<MappingResult> input = new ArrayList<>();
            input.add(validResult("a.xml", true, false, null));
            input.add(validResult("b.xml", false, false, null));

            final List<MappingResult> result = filter.filterScenarios(input);
            assertEquals(1, result.size());
            assertEquals("a.xml", result.get(0).file.getName());
        }

        @Test
        void allInactiveReturnsEmpty() {
            final List<MappingResult> input = new ArrayList<>();
            input.add(validResult("a.xml", false, false, null));

            final List<MappingResult> result = filter.filterScenarios(input);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class FilterOnlyThis {
        @Test
        void selectsOnlyThisScenarios() {
            final List<MappingResult> input = new ArrayList<>();
            input.add(validResult("a.xml", true, false, null));
            input.add(validResult("b.xml", true, true, null));

            final List<MappingResult> result = filter.filterScenarios(input);
            assertEquals(1, result.size());
            assertEquals("b.xml", result.get(0).file.getName());
        }

        @Test
        void noOnlyThisReturnsAllActive() {
            final List<MappingResult> input = new ArrayList<>();
            input.add(validResult("a.xml", true, false, null));
            input.add(validResult("b.xml", true, false, null));

            final List<MappingResult> result = filter.filterScenarios(input);
            assertEquals(2, result.size());
        }
    }

    @Nested
    class FilterByTags {
        @BeforeEach
        void enableTags() {
            final RunScenariosByTag runByTag = new RunScenariosByTag();
            runByTag.setEnabled(true);
            final TagValue tag = new TagValue();
            tag.setName("smoke");
            tag.setEnabled(true);
            runByTag.getTag().add(tag);
            when(config.getRunScenariosByTag()).thenReturn(runByTag);
        }

        @Test
        void filtersMatchingTags() {
            final List<MappingResult> input = new ArrayList<>();
            input.add(validResult("a.xml", true, false, "smoke,regression"));
            input.add(validResult("b.xml", true, false, "regression"));

            final List<MappingResult> result = filter.filterScenarios(input);
            assertEquals(1, result.size());
            assertEquals("a.xml", result.get(0).file.getName());
        }

        @Test
        void noMatchingTagsThrows() {
            final List<MappingResult> input = new ArrayList<>();
            input.add(validResult("a.xml", true, false, "regression"));

            assertThrows(DefaultFrameworkException.class, () -> filter.filterScenarios(input));
        }

        @Test
        void invalidScenarioParticipatesInTagFiltering() {
            final List<MappingResult> input = new ArrayList<>();
            input.add(invalidResult("a.xml", "regression"));
            input.add(validResult("b.xml", true, false, "smoke"));

            final List<MappingResult> result = filter.filterScenarios(input);
            assertEquals(1, result.size());
            assertEquals("b.xml", result.get(0).file.getName());
        }

        @Test
        void scenarioWithNullTagsIsExcluded() {
            final List<MappingResult> input = new ArrayList<>();
            input.add(validResult("a.xml", true, false, null));
            input.add(validResult("b.xml", true, false, "smoke"));

            final List<MappingResult> result = filter.filterScenarios(input);
            assertEquals(1, result.size());
            assertEquals("b.xml", result.get(0).file.getName());
        }
    }

    @Nested
    class NonParsedScenarios {
        @Test
        void nonParsedScenariosAreRemoved() {
            final List<MappingResult> input = new ArrayList<>();
            input.add(validResult("a.xml", true, false, null));
            input.add(nonParsedResult("bad.xml"));

            final List<MappingResult> result = filter.filterScenarios(input);
            assertEquals(1, result.size());
        }

        @Test
        void stopIfInvalidScenarioThrows() {
            when(config.isStopIfInvalidScenario()).thenReturn(true);
            final List<MappingResult> input = new ArrayList<>();
            input.add(nonParsedResult("bad.xml"));

            assertThrows(DefaultFrameworkException.class, () -> filter.filterScenarios(input));
        }

        @Test
        void emptyOriginalListThrows() {
            assertThrows(DefaultFrameworkException.class,
                    () -> filter.filterScenarios(new ArrayList<>()));
        }
    }

    @Nested
    class InvalidScenarios {
        @Test
        void invalidScenarioIsExcludedFromRun() {
            final List<MappingResult> input = new ArrayList<>();
            input.add(invalidResult("a.xml", null));
            input.add(validResult("b.xml", true, false, null));

            final List<MappingResult> result = filter.filterScenarios(input);
            assertEquals(1, result.size());
            assertEquals("b.xml", result.get(0).file.getName());
        }

        @Test
        void integrationDisabledScenarioStaysInRun() {
            final List<MappingResult> input = new ArrayList<>();
            input.add(invalidResult("a.xml", null, new IntegrationDisabledException("disabled")));

            final List<MappingResult> result = filter.filterScenarios(input);
            assertEquals(1, result.size());
            assertEquals("a.xml", result.get(0).file.getName());
        }

        @Test
        void allSelectedScenariosInvalidThrows() {
            final List<MappingResult> input = new ArrayList<>();
            input.add(invalidResult("a.xml", null));

            assertThrows(DefaultFrameworkException.class, () -> filter.filterScenarios(input));
            assertEquals("validation failed", ScenarioStatusRegistry.getInvalid().get("a.xml"));
        }

        @Test
        void stopIfInvalidScenarioThrowsForSelectedInvalid() {
            when(config.isStopIfInvalidScenario()).thenReturn(true);
            final List<MappingResult> input = new ArrayList<>();
            input.add(invalidResult("a.xml", null));
            input.add(validResult("b.xml", true, false, null));

            assertThrows(DefaultFrameworkException.class, () -> filter.filterScenarios(input));
        }

        @Test
        void stopIfInvalidScenarioIgnoresFilteredOutInvalid() {
            when(config.isStopIfInvalidScenario()).thenReturn(true);
            final List<MappingResult> input = new ArrayList<>();
            input.add(invalidResult("a.xml", null));
            input.add(validResult("b.xml", true, true, null));

            final List<MappingResult> result = filter.filterScenarios(input);
            assertEquals(1, result.size());
            assertEquals("b.xml", result.get(0).file.getName());
        }

        @Test
        void stopIfInvalidScenarioIgnoresIntegrationDisabled() {
            when(config.isStopIfInvalidScenario()).thenReturn(true);
            final List<MappingResult> input = new ArrayList<>();
            input.add(invalidResult("a.xml", null, new IntegrationDisabledException("disabled")));

            final List<MappingResult> result = filter.filterScenarios(input);
            assertEquals(1, result.size());
        }
    }

    @Nested
    class StatusRegistry {

        @Test
        void inactiveScenarioIsRegisteredAsSkipped() {
            final List<MappingResult> input = new ArrayList<>();
            input.add(validResult("a.xml", true, false, null));
            input.add(validResult("b.xml", false, false, null));

            filter.filterScenarios(input);

            assertEquals(1, ScenarioStatusRegistry.getSkipped().size());
            assertEquals(LogMessage.SCENARIO_SKIPPED_INACTIVE,
                    ScenarioStatusRegistry.getSkipped().get("b.xml"));
            assertTrue(ScenarioStatusRegistry.getInvalid().isEmpty());
        }

        @Test
        void scenariosLosingToOnlyThisAreRegisteredAsSkipped() {
            final List<MappingResult> input = new ArrayList<>();
            input.add(validResult("a.xml", true, false, null));
            input.add(validResult("b.xml", true, true, null));

            filter.filterScenarios(input);

            assertEquals(1, ScenarioStatusRegistry.getSkipped().size());
            assertEquals(LogMessage.SCENARIO_SKIPPED_ONLY_THIS,
                    ScenarioStatusRegistry.getSkipped().get("a.xml"));
        }

        @Test
        void nothingIsRegisteredWhenOnlyThisIsAbsent() {
            final List<MappingResult> input = new ArrayList<>();
            input.add(validResult("a.xml", true, false, null));
            input.add(validResult("b.xml", true, false, null));

            filter.filterScenarios(input);

            assertTrue(ScenarioStatusRegistry.getSkipped().isEmpty());
        }

        @Test
        void nonMatchingAndMissingTagsAreRegisteredAsSkipped() {
            final RunScenariosByTag runByTag = new RunScenariosByTag();
            runByTag.setEnabled(true);
            final TagValue tag = new TagValue();
            tag.setName("smoke");
            tag.setEnabled(true);
            runByTag.getTag().add(tag);
            when(config.getRunScenariosByTag()).thenReturn(runByTag);

            final List<MappingResult> input = new ArrayList<>();
            input.add(validResult("a.xml", true, false, "smoke"));
            input.add(validResult("b.xml", true, false, "regression"));
            input.add(validResult("c.xml", true, false, null));

            filter.filterScenarios(input);

            assertEquals(2, ScenarioStatusRegistry.getSkipped().size());
            assertEquals(LogMessage.SCENARIO_SKIPPED_TAGS_NOT_MATCH,
                    ScenarioStatusRegistry.getSkipped().get("b.xml"));
            assertEquals(LogMessage.SCENARIO_SKIPPED_WITHOUT_TAGS,
                    ScenarioStatusRegistry.getSkipped().get("c.xml"));
        }

        @Test
        void nonParsedScenarioIsRegisteredAsInvalid() {
            final List<MappingResult> input = new ArrayList<>();
            input.add(validResult("a.xml", true, false, null));
            input.add(nonParsedResult("bad.xml"));

            filter.filterScenarios(input);

            assertEquals(1, ScenarioStatusRegistry.getInvalid().size());
            assertEquals("parse error", ScenarioStatusRegistry.getInvalid().get("bad.xml"));
            assertTrue(ScenarioStatusRegistry.getSkipped().isEmpty());
        }

        @Test
        void scenarioCarryingAnExceptionIsRegisteredAsInvalid() {
            final List<MappingResult> input = new ArrayList<>();
            input.add(invalidResult("a.xml", null, new RuntimeException("integration disabled")));
            input.add(validResult("b.xml", true, false, null));

            filter.filterScenarios(input);

            assertEquals(1, ScenarioStatusRegistry.getInvalid().size());
            assertEquals("integration disabled", ScenarioStatusRegistry.getInvalid().get("a.xml"));
        }

        @Test
        void invalidScenarioFilteredOutByTagsIsSkippedNotInvalid() {
            final RunScenariosByTag runByTag = new RunScenariosByTag();
            runByTag.setEnabled(true);
            final TagValue tag = new TagValue();
            tag.setName("smoke");
            tag.setEnabled(true);
            runByTag.getTag().add(tag);
            when(config.getRunScenariosByTag()).thenReturn(runByTag);

            final List<MappingResult> input = new ArrayList<>();
            input.add(invalidResult("a.xml", "regression"));
            input.add(validResult("b.xml", true, false, "smoke"));

            filter.filterScenarios(input);

            assertTrue(ScenarioStatusRegistry.getInvalid().isEmpty());
            assertEquals(LogMessage.SCENARIO_SKIPPED_TAGS_NOT_MATCH,
                    ScenarioStatusRegistry.getSkipped().get("a.xml"));
        }

        @Test
        void invalidScenarioSkippedByOnlyThisIsNotRegisteredAsInvalid() {
            final List<MappingResult> input = new ArrayList<>();
            input.add(invalidResult("a.xml", null));
            input.add(validResult("b.xml", true, true, null));

            filter.filterScenarios(input);

            assertTrue(ScenarioStatusRegistry.getInvalid().isEmpty());
            assertEquals(LogMessage.SCENARIO_SKIPPED_ONLY_THIS,
                    ScenarioStatusRegistry.getSkipped().get("a.xml"));
        }

        @Test
        void inactiveInvalidScenarioIsNotRegisteredAsInvalid() {
            final List<MappingResult> input = new ArrayList<>();
            final MappingResult parsed = validResult("a.xml", false, false, null);
            input.add(new MappingResult(parsed.file, parsed.scenario, new RuntimeException("validation failed")));
            input.add(validResult("b.xml", true, false, null));

            filter.filterScenarios(input);

            assertTrue(ScenarioStatusRegistry.getInvalid().isEmpty());
            assertEquals(LogMessage.SCENARIO_SKIPPED_INACTIVE,
                    ScenarioStatusRegistry.getSkipped().get("a.xml"));
        }
    }

    @Nested
    class TagTable {

        private Map<String, Long> capturedCounts() {
            final ArgumentCaptor<Map<String, Long>> captor = ArgumentCaptor.forClass(Map.class);
            verify(configurationLogger).logTagConfiguration(any(), captor.capture());
            return captor.getValue();
        }

        @Test
        void countsEveryParsedScenarioPerTag() {
            final List<MappingResult> input = new ArrayList<>();
            input.add(validResult("a.xml", true, false, "smoke"));
            input.add(validResult("b.xml", true, false, "smoke,regression"));

            filter.filterScenarios(input);

            assertEquals(Map.of("smoke", 2L, "regression", 1L), capturedCounts());
        }

        @Test
        void countsInactiveScenariosToo() {
            final List<MappingResult> input = new ArrayList<>();
            input.add(validResult("a.xml", true, false, "smoke"));
            input.add(validResult("b.xml", false, false, "smoke"));

            filter.filterScenarios(input);

            assertEquals(Map.of("smoke", 2L), capturedCounts());
        }

        @Test
        void skipsNonParsedScenariosAndScenariosWithoutTags() {
            final List<MappingResult> input = new ArrayList<>();
            input.add(validResult("a.xml", true, false, "smoke"));
            input.add(validResult("b.xml", true, false, null));
            input.add(nonParsedResult("bad.xml"));

            filter.filterScenarios(input);

            assertEquals(Map.of("smoke", 1L), capturedCounts());
        }

        @Test
        void doesNotTrimTagsSoTheTableMatchesTheFilter() {
            final List<MappingResult> input = new ArrayList<>();
            input.add(validResult("a.xml", true, false, "smoke, regression"));

            filter.filterScenarios(input);

            assertEquals(Map.of("smoke", 1L, " regression", 1L), capturedCounts());
        }

        @Test
        void isLoggedBeforeTagFilteringBlowsUp() {
            final RunScenariosByTag runByTag = new RunScenariosByTag();
            runByTag.setEnabled(true);
            final TagValue tag = new TagValue();
            tag.setName("smoke");
            tag.setEnabled(true);
            runByTag.getTag().add(tag);
            when(config.getRunScenariosByTag()).thenReturn(runByTag);

            final List<MappingResult> input = new ArrayList<>();
            input.add(validResult("a.xml", true, false, "regression"));

            assertThrows(DefaultFrameworkException.class, () -> filter.filterScenarios(input));
            assertEquals(Map.of("regression", 1L), capturedCounts());
        }
    }

    @Nested
    class TagConfiguration {
        @Test
        void noEnabledTagsThrows() {
            final RunScenariosByTag runByTag = new RunScenariosByTag();
            runByTag.setEnabled(true);
            final TagValue tag = new TagValue();
            tag.setName("smoke");
            tag.setEnabled(false);
            runByTag.getTag().add(tag);
            when(config.getRunScenariosByTag()).thenReturn(runByTag);

            final List<MappingResult> input = new ArrayList<>();
            input.add(validResult("a.xml", true, false, "smoke"));

            assertThrows(DefaultFrameworkException.class, () -> filter.filterScenarios(input));
        }
    }
}

package com.knubisoft.testlum.testing.framework.scenario;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioCollector.MappingResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.testlum.testing.model.global_config.RunScenariosByTag;
import com.knubisoft.testlum.testing.model.global_config.TagValue;
import com.knubisoft.testlum.testing.model.scenario.Scenario;
import com.knubisoft.testlum.testing.model.scenario.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ScenarioFilterTest {

    private GlobalTestConfiguration config;
    private LogUtil logUtil;
    private ScenarioFilter filter;

    @BeforeEach
    void setUp() {
        config = mock(GlobalTestConfiguration.class);
        logUtil = mock(LogUtil.class);
        doNothing().when(logUtil).logScenarioWithoutTags(anyString());
        filter = new ScenarioFilter(config, logUtil);

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

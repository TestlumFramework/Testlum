package com.testlum.testing.logger;

import com.testlum.log.table.DynamicTableBuilder;
import com.testlum.testing.model.global_config.RunScenariosByTag;
import com.testlum.testing.model.global_config.TagValue;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationLoggerTagTest {

    /**
     * Records whether the table was ever filled in. Mockito is not a dependency of this module,
     * so the spy is hand rolled.
     */
    private static final class RecordingTagLogger extends TagConfigurationLogger {

        private boolean called;

        @Override
        public void appendSections(final DynamicTableBuilder table,
                                   final RunScenariosByTag runScenariosByTag,
                                   final Map<String, Long> scenarioCountByTag) {
            this.called = true;
            super.appendSections(table, runScenariosByTag, scenarioCountByTag);
        }
    }

    private RunScenariosByTag config(final boolean enabled) {
        RunScenariosByTag config = new RunScenariosByTag();
        config.setEnabled(enabled);
        TagValue tag = new TagValue();
        tag.setName("smoke");
        tag.setEnabled(true);
        config.getTag().add(tag);
        return config;
    }

    private RecordingTagLogger run(final RunScenariosByTag config) {
        RecordingTagLogger tagLogger = new RecordingTagLogger();
        new ConfigurationLogger(new UiConfigurationLogger(), new IntegrationConfigurationLogger(), tagLogger)
                .logTagConfiguration(config, Map.of("smoke", 3L));
        return tagLogger;
    }

    @Test
    void buildsTheTableWhenTagFilteringIsEnabled() {
        assertTrue(run(config(true)).called);
    }

    @Test
    void skipsTheTableWhenTagFilteringIsDisabled() {
        assertFalse(run(config(false)).called);
    }

    @Test
    void skipsTheTableWhenThereIsNoTagConfigurationAtAll() {
        assertFalse(run(null).called);
    }
}

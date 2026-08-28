package com.testlum.testing.logger;

import com.testlum.log.table.DynamicTableBuilder;
import com.testlum.log.table.TableBuilder;
import com.testlum.testing.framework.constant.LogMessage;
import com.testlum.testing.model.global_config.RunScenariosByTag;
import com.testlum.testing.model.global_config.TagValue;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TagConfigurationLoggerTest {

    private final TagConfigurationLogger logger = new TagConfigurationLogger();

    private DynamicTableBuilder table() {
        return TableBuilder.grid(LogMessage.TAG_CONFIG_TABLE_TITLE)
                .columnCount(TagConfigurationLogger.COLUMN_COUNT);
    }

    private RunScenariosByTag config(final String... nameAndState) {
        RunScenariosByTag config = new RunScenariosByTag();
        config.setEnabled(true);
        for (int i = 0; i < nameAndState.length; i += 2) {
            TagValue tag = new TagValue();
            tag.setName(nameAndState[i]);
            tag.setEnabled(Boolean.parseBoolean(nameAndState[i + 1]));
            config.getTag().add(tag);
        }
        return config;
    }

    private Map<String, Long> counts(final Object... nameAndCount) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (int i = 0; i < nameAndCount.length; i += 2) {
            result.put((String) nameAndCount[i], ((Number) nameAndCount[i + 1]).longValue());
        }
        return result;
    }

    @Nested
    class DeclaredTags {

        @Test
        void rendersEveryDeclaredTagWithItsCount() {
            DynamicTableBuilder table = table();

            logger.appendSections(table, config("smoke", "true", "regression", "false"),
                    counts("smoke", 12, "regression", 5));
            String rendered = table.build();

            assertTrue(rendered.contains(LogMessage.TAG_CONFIG_TABLE_TAG_HEADER));
            assertTrue(rendered.contains("smoke"));
            assertTrue(rendered.contains("12"));
            assertTrue(rendered.contains("regression"));
            assertTrue(rendered.contains("5"));
        }

        @Test
        void declaredTagNoScenarioUsesShowsZero() {
            DynamicTableBuilder table = table();

            logger.appendSections(table, config("unused", "true"), counts());
            String rendered = table.build();

            assertTrue(rendered.contains("unused"));
            assertTrue(rendered.contains("0"));
        }

        @Test
        void writesOneHeaderRowPlusOneRowPerTag() {
            DynamicTableBuilder table = table();

            logger.appendSections(table, config("a", "true", "b", "false"), counts("a", 1));

            assertEquals(3, table.getRows().size());
        }
    }

    @Nested
    class UndeclaredTags {

        @Test
        void tagsMissingFromConfigGetTheirOwnSection() {
            DynamicTableBuilder table = table();

            logger.appendSections(table, config("smoke", "true"), counts("smoke", 12, "smoke_2", 1));
            String rendered = table.build();

            assertTrue(rendered.contains(LogMessage.TAG_CONFIG_TABLE_UNDECLARED_ROW));
            assertTrue(rendered.contains("smoke_2"));
            assertTrue(rendered.contains(LogMessage.TAG_CONFIG_TABLE_UNDECLARED_ENABLED_CELL));
        }

        @Test
        void sectionIsAbsentWhenEveryUsedTagIsDeclared() {
            DynamicTableBuilder table = table();

            logger.appendSections(table, config("smoke", "true"), counts("smoke", 12));
            String rendered = table.build();

            assertFalse(rendered.contains(LogMessage.TAG_CONFIG_TABLE_UNDECLARED_ROW));
        }

        @Test
        void untrimmedTagFromAScenarioSurfacesAsUndeclared() {
            DynamicTableBuilder table = table();

            logger.appendSections(table, config("regression", "true"), counts(" regression", 3));
            String rendered = table.build();

            assertTrue(rendered.contains(LogMessage.TAG_CONFIG_TABLE_UNDECLARED_ROW));
        }
    }

    @Nested
    class NothingToRender {

        @Test
        void appendsNoRowsWhenThereAreNoTagsAtAll() {
            DynamicTableBuilder table = table();

            logger.appendSections(table, config(), counts());

            assertTrue(table.getRows().isEmpty());
        }
    }
}

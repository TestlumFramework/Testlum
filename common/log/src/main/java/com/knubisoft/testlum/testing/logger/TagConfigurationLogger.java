package com.knubisoft.testlum.testing.logger;

import com.knubisoft.testlum.log.Color;
import com.knubisoft.testlum.log.table.DynamicTableBuilder;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.model.global_config.RunScenariosByTag;
import com.knubisoft.testlum.testing.model.global_config.TagValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TagConfigurationLogger {

    public static final int COLUMN_COUNT = 3;

    public void appendSections(final DynamicTableBuilder table,
                               final RunScenariosByTag runScenariosByTag,
                               final Map<String, Long> scenarioCountByTag) {
        List<TagValue> declaredTags = runScenariosByTag.getTag();
        if (declaredTags.isEmpty() && scenarioCountByTag.isEmpty()) {
            return;
        }
        writeHeader(table);
        declaredTags.forEach(tag -> table.row(computeRowColor(tag.isEnabled()),
                tag.getName(),
                tag.isEnabled(),
                scenarioCountByTag.getOrDefault(tag.getName(), 0L)));
        appendUndeclaredSection(table, declaredTags, scenarioCountByTag);
    }

    private void appendUndeclaredSection(final DynamicTableBuilder table,
                                         final List<TagValue> declaredTags,
                                         final Map<String, Long> scenarioCountByTag) {
        Set<String> declaredNames = declaredTags.stream().map(TagValue::getName).collect(Collectors.toSet());
        List<Map.Entry<String, Long>> undeclared = scenarioCountByTag.entrySet().stream()
                .filter(entry -> !declaredNames.contains(entry.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .toList();
        if (undeclared.isEmpty()) {
            return;
        }
        table.span(LogMessage.TAG_CONFIG_TABLE_UNDECLARED_ROW);
        undeclared.forEach(entry -> table.row(Color.ORANGE,
                entry.getKey(),
                LogMessage.TAG_CONFIG_TABLE_UNDECLARED_ENABLED_CELL,
                entry.getValue()));
    }

    private void writeHeader(final DynamicTableBuilder table) {
        table.row(LogMessage.TAG_CONFIG_TABLE_TAG_HEADER,
                LogMessage.TAG_CONFIG_TABLE_ENABLED_HEADER,
                LogMessage.TAG_CONFIG_TABLE_SCENARIOS_HEADER);
    }

    private Color computeRowColor(final boolean isEnabled) {
        if (isEnabled) {
            return Color.GREEN;
        }
        return Color.RED;
    }
}

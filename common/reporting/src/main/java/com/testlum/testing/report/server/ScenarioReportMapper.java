package com.testlum.testing.report.server;

import com.testlum.reporting.sdk.model.scenario.CommandError;
import com.testlum.reporting.sdk.model.scenario.CommandReport;
import com.testlum.reporting.sdk.model.scenario.Overview;
import com.testlum.reporting.sdk.model.scenario.ScenarioReport;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.report.ScenarioResult;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ScenarioReportMapper {

    public ScenarioReport map(final ScenarioResult result) {
        return withPlatform(ScenarioReport.builder(), result)
                .id(result.getId())
                .overview(mapOverview(result.getOverview()))
                .name(result.getName())
                .tags(result.getTags())
                .path(result.getPath())
                .success(result.isSuccess()).skipped(result.isSkipped())
                .cause(result.getCause())
                .startedAt(result.getStartedAt()).executionTime(result.getExecutionTime())
                .environment(result.getEnvironment()).variation(result.getVariation())
                .commands(mapCommands(result.getCommands()))
                .build();
    }

    private ScenarioReport.ScenarioReportBuilder withPlatform(final ScenarioReport.ScenarioReportBuilder builder,
                                                              final ScenarioResult result) {
        return builder
                .browser(result.getBrowser())
                .mobilebrowserDevice(result.getMobilebrowserDevice())
                .nativeDevice(result.getNativeDevice());
    }

    private Overview mapOverview(final com.testlum.testing.model.scenario.Overview overview) {
        if (overview == null) {
            return null;
        }
        return Overview.builder()
                .name(overview.getName())
                .description(overview.getDescription())
                .jira(overview.getJira())
                .developer(overview.getDeveloper())
                .link(overview.getLink())
                .build();
    }

    private List<CommandReport> mapCommands(final List<CommandResult> commands) {
        return commands == null ? null : commands.stream().map(this::mapCommand).toList();
    }

    private CommandReport mapCommand(final CommandResult command) {
        return CommandReport.builder()
                .id(command.getId())
                .commandKey(command.getCommandKey())
                .comment(command.getComment())
                .expected(command.getExpected()).actual(command.getActual())
                .success(command.isSuccess()).skipped(command.isSkipped())
                .error(mapError(command.getException()))
                .executionTime(command.getExecutionTime())
                .base64Screenshot(command.getBase64Screenshot())
                .subCommands(mapCommands(command.getSubCommandsResult()))
                .metadata(mapMetadata(command.getMetadata()))
                .build();
    }

    private CommandError mapError(final Exception exception) {
        if (exception == null) {
            return null;
        }
        return CommandError.builder()
                .type(exception.getClass().getName())
                .message(exception.getMessage())
                .build();
    }

    private Map<String, Object> mapMetadata(final Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        Map<String, Object> wireMetadata = new LinkedHashMap<>();
        metadata.forEach((key, value) -> wireMetadata.put(key, toWireValue(value)));
        return wireMetadata;
    }

    private Object toWireValue(final Object value) {
        if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean) {
            return value;
        }
        if (value instanceof Collection<?> values) {
            return values.stream().map(String::valueOf).toList();
        }
        return String.valueOf(value);
    }
}

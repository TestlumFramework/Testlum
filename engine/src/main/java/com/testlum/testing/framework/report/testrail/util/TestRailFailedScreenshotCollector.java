package com.testlum.testing.framework.report.testrail.util;

import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.report.ScenarioResult;
import com.testlum.testing.framework.report.testrail.model.ScenarioCase;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TestRailFailedScreenshotCollector {

    public Map<Integer, String> getScreenshotsOfUnsuccessfulTests(final List<ScenarioCase> scenarioCases) {
        Map<Integer, String> caseIdAttachmentsMap = new HashMap<>();
        scenarioCases.forEach(scenarioCase -> {
            String screenshotOfLastUnsuccessfulCommand =
                    getScreenshotOfLastUnsuccessfulCommand(scenarioCase.scenarioResult());
            if (screenshotOfLastUnsuccessfulCommand != null) {
                caseIdAttachmentsMap.put(scenarioCase.caseId(), screenshotOfLastUnsuccessfulCommand);
            }
        });
        return caseIdAttachmentsMap;
    }

    private String getScreenshotOfLastUnsuccessfulCommand(final ScenarioResult scenarioResult) {
        List<CommandResult> result = new ArrayList<>();
        scenarioResult.getCommands().forEach(commend ->
                collectUnsuccessfulCommandsRecursive(commend, result));
        return result.isEmpty() ? null : result.get(result.size() - 1).getBase64Screenshot();
    }

    private void collectUnsuccessfulCommandsRecursive(final CommandResult command, final List<CommandResult> result) {
        if (command == null) {
            return;
        }
        if (command.getBase64Screenshot() != null && !command.isSuccess()) {
            result.add(command);
        }
        if (command.getSubCommandsResult() != null) {
            for (CommandResult subCommand : command.getSubCommandsResult()) {
                collectUnsuccessfulCommandsRecursive(subCommand, result);
            }
        }
    }

}
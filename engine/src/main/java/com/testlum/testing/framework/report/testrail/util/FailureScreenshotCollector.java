package com.testlum.testing.framework.report.testrail.util;

import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.report.ScenarioResult;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FailureScreenshotCollector {

    public Map<Integer, String> getScreenshotsOfUnsuccessfulTests(final List<ScenarioResult> scenarioResults) {
        Map<Integer, String> caseIdAttachmentsMap = new HashMap<>();
        scenarioResults.forEach(scenarioResult -> {
            String screenshotOfLastUnsuccessfulCommand = getScreenshotOfLastUnsuccessfulCommand(scenarioResult);
            if (screenshotOfLastUnsuccessfulCommand != null) {
                String testCaseId = scenarioResult.getOverview().getTestRail().getTestCaseId();
                if (NumberUtils.isParsable(testCaseId)) {
                    caseIdAttachmentsMap.put(Integer.parseInt(testCaseId), screenshotOfLastUnsuccessfulCommand);
                }
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
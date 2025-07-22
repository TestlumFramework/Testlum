package com.knubisoft.testlum.testing.framework.testRail.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.report.ScenarioResult;
import com.knubisoft.testlum.testing.framework.testRail.constant.TestRailConstants;
import com.knubisoft.testlum.testing.framework.testRail.model.GroupedScenarios;
import com.knubisoft.testlum.testing.framework.testRail.model.ResultResponseDto;
import com.knubisoft.testlum.testing.model.global_config.TestRailReports;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@UtilityClass
public class TestRailUtil {

    public List<ScenarioResult> getScenarioWithTestRailIntegrations(final List<ScenarioResult> allScenarioResults) {
        return allScenarioResults.stream()
                .filter(scenarioResult -> scenarioResult.getOverview().getTestRails().isEnable())
                .collect(Collectors.toList());
    }

    public GroupedScenarios splitScenariosByRunId(final List<ScenarioResult> scenarioResults) {
        Map<Integer, List<ScenarioResult>> withRunId = getAllScenarioWithRunId(scenarioResults);
        List<ScenarioResult> withoutRunId = getAllScenarioWithoutRunId(scenarioResults);
        return new GroupedScenarios(withRunId, withoutRunId);
    }

    private List<ScenarioResult> getAllScenarioWithoutRunId(final List<ScenarioResult> scenarioResults) {
        return scenarioResults.stream()
                .filter(scenarioResult -> {
                    var testRails = scenarioResult.getOverview().getTestRails();
                    Boolean enable = testRails.isEnable();
                    String runId = testRails.getTestRailRunId();
                    String testCase = testRails.getTestCaseId();
                    return Boolean.TRUE.equals(enable)
                            && (StringUtils.isEmpty(runId) || Integer.parseInt(runId) <= 0)
                            && (NumberUtils.isParsable(testCase) && Integer.parseInt(testCase) > 0);
                })
                .collect(Collectors.toList());
    }

    public List<Integer> extractCaseIds(List<ScenarioResult> scenarioResults) {
        return scenarioResults.stream()
                .map(scenarioResult -> scenarioResult.getOverview().getTestRails().getTestCaseId())
                .filter(id -> parseId(id, "caseId"))
                .map(Integer::parseInt)
                .filter(caseId -> caseId > 0)
                .distinct()
                .collect(Collectors.toList());
    }


    private Map<Integer, List<ScenarioResult>> getAllScenarioWithRunId(final List<ScenarioResult> scenarioResults) {
        return scenarioResults.stream()
                .filter(scenarioResult -> {
                    Boolean enable = scenarioResult.getOverview().getTestRails().isEnable();
                    String runId = scenarioResult.getOverview().getTestRails().getTestRailRunId();
                    return Boolean.TRUE.equals(enable) && parseId(runId, "runId") && Integer.parseInt(runId) > 0;
                })
                .collect(Collectors.groupingBy(
                        scenarioResult -> Integer.parseInt(scenarioResult.getOverview().getTestRails().getTestRailRunId())
                ));
    }

    public static List<Map<String, Object>> buildBatchResults(final List<ScenarioResult> scenarioList) {
        return scenarioList.stream()
                .map(scenarioResult -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put(TestRailConstants.CASE_ID, scenarioResult.getOverview().getTestRails().getTestCaseId());
                    result.put(TestRailConstants.STATUS_ID, determineStatus(scenarioResult));
                    result.put(TestRailConstants.COMMENT, generateComment(scenarioResult));
                    return result;
                })
                .collect(Collectors.toList());
    }

    private static int determineStatus(final ScenarioResult scenarioResult) {
        return scenarioResult.isSuccess()
                ? TestRailConstants.STATUS_PASSED
                : TestRailConstants.STATUS_FAILED;
    }

    private static String generateComment(final ScenarioResult scenarioResult) {
        String scenarioName = scenarioResult.getOverview().getName();
        String cause = scenarioResult.getCause();

        return (cause != null && !cause.isEmpty())
                ? String.format(TestRailConstants.COMMENT_FAILED_TEMPLATE, scenarioName, cause)
                : String.format(TestRailConstants.COMMENT_PASSED_TEMPLATE, scenarioName);
    }

    public static Map<String, Object> buildCreateTestRunRequest(final TestRailReports testRails,
                                                                final List<Integer> caseIds) {
        Map<String, Object> request = new HashMap<>();
        request.put(TestRailConstants.RUN_NAME, testRails.getDefaultRunName());
        request.put(TestRailConstants.RUN_DESCRIPTION, testRails.getDefaultRunDescription());
        request.put(TestRailConstants.RUN_INCLUDE_ALL, false);
        request.put(TestRailConstants.RUN_CASE_IDS, caseIds);
        return request;
    }

    private boolean parseId(final String idStr, final String idType) {
        if (NumberUtils.isParsable(idStr)) {
            return Boolean.TRUE;
        } else {
            String idLogError = idType.equalsIgnoreCase("caseId") ? TestRailConstants.CASE_ID_ERROR_RESPONSE
                    : TestRailConstants.RUN_ID_ERROR_RESPONSE;
            log.error(idLogError, idStr);
            return Boolean.FALSE;
        }
    }

    public static Map<Integer, String> getScreenshotsOfUnsuccessfulTests(final List<ScenarioResult> scenarioResults) {
        Map<Integer, String> caseIdAttachmentsMap = new HashMap<>();
        scenarioResults.forEach(scenarioResult -> {
            String screenshotOfLastUnsuccessfulCommand = getScreenshotOfLastUnsuccessfulCommand(scenarioResult);
            if (screenshotOfLastUnsuccessfulCommand != null) {
                caseIdAttachmentsMap.put(
                        Integer.parseInt(scenarioResult.getOverview().getTestRails().getTestCaseId()),
                        screenshotOfLastUnsuccessfulCommand
                );
            }
        });
        return caseIdAttachmentsMap;
    }

    private static String getScreenshotOfLastUnsuccessfulCommand(final ScenarioResult scenarioResult) {
        List<CommandResult> result = new ArrayList<>();
        scenarioResult.getCommands().forEach(c -> collectUnsuccessfulCommandsRecursive(c, result));
        return result.isEmpty() ? null : result.get(result.size() - 1).getBase64Screenshot();
    }

    private static void collectUnsuccessfulCommandsRecursive(CommandResult command, List<CommandResult> result) {
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

    public static List<ResultResponseDto> extractResultsDTOs(String jsonResponse) {
        List<ResultResponseDto> results = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(jsonResponse);
            if (root.isArray()) {
                for (JsonNode resultNode : root) {
                    ResultResponseDto resultDto = new ResultResponseDto();
                    resultDto.setId(resultNode.get("id").asInt());
                    resultDto.setTestId(resultNode.get("test_id").asInt());
                    resultDto.setStatusId(resultNode.get("status_id").asInt());
                    results.add(resultDto);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    public static Integer extractIdFieldFromJson(String jsonResponse, String idField) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(jsonResponse);
            if (!root.isArray()) {
                JsonNode idNode = root.get(idField);
                if (idNode != null && idNode.isInt()) {
                    return idNode.asInt();
                }
            }
        } catch (Exception e) {
            log.error(TestRailConstants.ID_FETCH_ERROR_RESPONSE, idField);
            return null;
        }
        return null;
    }
}

package com.knubisoft.testlum.testing.framework.testRail.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.report.ScenarioResult;
import com.knubisoft.testlum.testing.framework.testRail.constant.TestRailConstants;
import com.knubisoft.testlum.testing.framework.testRail.model.GroupedScenarios;
import com.knubisoft.testlum.testing.framework.testRail.model.ResultResponseDto;
import com.knubisoft.testlum.testing.framework.testRail.model.Run;
import com.knubisoft.testlum.testing.model.global_config.TestRailReports;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.ERROR_ON_PARSING_JSON;

@Slf4j
@UtilityClass
public class TestRailUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Pattern EX_PREFIX = Pattern.compile(
            "^(?:(?:[\\p{Alpha}_$][\\w$]*\\.)*[\\p{Alpha}_$][\\w$]*(?:Exception|Error|Failure|Throwable)\\s*:\\s*)+"
    );
    private static final Pattern STACK_TRACE_LINE = Pattern.compile(
            "(?m)^\\s*(?:at\\s+.+|Suppressed:\\s+.+|\\.\\.\\.\\s+\\d+\\s+more)\\s*$"
    );

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

    public List<Map<String, Object>> buildBatchResults(final List<ScenarioResult> scenarioList) {
        return scenarioList.stream()
                .map(scenarioResult -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put(TestRailConstants.CASE_ID, scenarioResult.getOverview().getTestRails().getTestCaseId());
                    result.put(TestRailConstants.STATUS_ID, determineStatus(scenarioResult));
                    result.put(TestRailConstants.COMMENT, generateComment(scenarioResult));
					result.put(TestRailConstants.ELAPSED, durationOfExecution(scenarioResult.getExecutionTime()));
                    return result;
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

    public Run buildTestRunRequest(final TestRailReports testRails,
                                   final List<Integer> caseIds) {
	    return Run.builder()
			    .name(testRails.getDefaultRunName())
			    .description(testRails.getDefaultRunDescription())
			    .includeAll(false)
			    .caseIds(caseIds).build();
    }

    public Map<Integer, String> getScreenshotsOfUnsuccessfulTests(final List<ScenarioResult> scenarioResults) {
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

    public List<ResultResponseDto> extractResultsDTOs(String jsonResponse) {
        List<ResultResponseDto> results = new ArrayList<>();
        try {
            JsonNode root = OBJECT_MAPPER.readTree(jsonResponse);
            if (root.isArray()) {
                for (JsonNode resultNode : root) {
                    ResultResponseDto resultDto = ResultResponseDto.builder()
                            .id(resultNode.get("id").asInt())
                            .testId(resultNode.get("test_id").asInt())
                            .statusId(resultNode.get("status_id").asInt())
                            .build();
                    results.add(resultDto);
                }
            }
        } catch (Exception e) {
            throw new DefaultFrameworkException(ERROR_ON_PARSING_JSON, e);
        }

        return results;
    }

    public Integer extractIdFieldFromJson(String jsonResponse, String idField) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(jsonResponse);
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

	public String durationOfExecution(long executionTime) {
		long totalSeconds = executionTime / 1000;
		long hours = totalSeconds / 3600;
		long minutes = (totalSeconds % 3600) / 60;
		long seconds = totalSeconds % 60;

		StringBuilder duration = new StringBuilder();
		if (hours > 0) duration.append(hours).append("h ");
		if (minutes > 0) duration.append(minutes).append("m ");
		if (seconds > 0) duration.append(seconds).append("s");

		return duration.toString().trim();
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

    private boolean parseId(final String idStr, final String idType) {
        if (NumberUtils.isParsable(idStr)) {
            return Boolean.TRUE;
        } else {
            String idLogError = "caseId".equalsIgnoreCase(idType)
                    ? TestRailConstants.CASE_ID_ERROR_RESPONSE
                    : TestRailConstants.RUN_ID_ERROR_RESPONSE;
            log.error(idLogError, idStr);
            return Boolean.FALSE;
        }
    }

    private int determineStatus(final ScenarioResult scenarioResult) {
        return scenarioResult.isSuccess()
                ? TestRailConstants.STATUS_PASSED
                : TestRailConstants.STATUS_FAILED;
    }

    private String generateComment(final ScenarioResult scenarioResult) {
        String scenarioName = scenarioResult.getOverview().getName();
        String cause = sanitizeFailureMessage(scenarioResult.getCause());
        return StringUtils.isNotBlank(cause)
                ? String.format(TestRailConstants.COMMENT_FAILED_TEMPLATE, scenarioName, cause)
                : String.format(TestRailConstants.COMMENT_PASSED_TEMPLATE, scenarioName);
    }

    private String getScreenshotOfLastUnsuccessfulCommand(final ScenarioResult scenarioResult) {
        List<CommandResult> result = new ArrayList<>();
        scenarioResult.getCommands().forEach(c -> collectUnsuccessfulCommandsRecursive(c, result));
        return result.isEmpty() ? null : result.get(result.size() - 1).getBase64Screenshot();
    }

    private void collectUnsuccessfulCommandsRecursive(CommandResult command, List<CommandResult> result) {
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

    private static String sanitizeFailureMessage(final String cause) {
        if (StringUtils.isBlank(cause)) return "";

        String msg = cause.replace('\r', '\n');

        int idx = msg.lastIndexOf("Caused by:");
        if (idx >= 0) {
            msg = msg.substring(idx + "Caused by:".length()).trim();
        }

        msg = STACK_TRACE_LINE.matcher(msg).replaceAll("");
        msg = EX_PREFIX.matcher(msg).replaceFirst("");
        msg = msg.replace('\n', ' ').replaceAll("\\s+", " ").trim();

        return msg;
    }
}

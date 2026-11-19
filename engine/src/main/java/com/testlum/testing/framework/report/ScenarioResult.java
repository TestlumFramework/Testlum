package com.testlum.testing.framework.report;

import com.testlum.testing.model.scenario.Overview;
import lombok.Data;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Data
public class ScenarioResult {

    private int id;
    private Overview overview;
    private String name;
    private String tags;
    private String path;
    private String browser;
    private String mobilebrowserDevice;
    private String nativeDevice;

    private boolean success;
    private String cause;
    private FailedStep failedStep;
    private long executionTime;
    private String environment;

    private List<CommandResult> commands = new ArrayList<>();

    /** A scenario that threw outside any step, for example while its overview was being injected. */
    public static ScenarioResult failedOutsideStep(final String path, final Throwable error) {
        ScenarioResult result = new ScenarioResult();
        String cause = causeOf(error);
        result.setPath(path);
        result.setName(scenarioFolder(path));
        result.setOverview(overviewFor(result.getName(), cause));
        result.setSuccess(false);
        result.setCause(cause);
        result.setFailedStep(FailedStep.beforeFirstStep(error));
        return result;
    }

    // The scenario's own overview could not be read, so the report readers get a minimal one that names the failure.
    private static Overview overviewFor(final String name, final String cause) {
        Overview overview = new Overview();
        overview.setName(name);
        overview.setDescription("Failed before its first step: " + cause);
        return overview;
    }

    private static String scenarioFolder(final String path) {
        Path parent = Paths.get(path).getParent();
        return parent == null || parent.getFileName() == null ? path : parent.getFileName().toString();
    }

    private static String causeOf(final Throwable error) {
        String message = error.getMessage();
        return message == null || message.isBlank() ? error.getClass().getSimpleName() : message;
    }
}

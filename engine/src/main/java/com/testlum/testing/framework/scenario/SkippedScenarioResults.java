package com.testlum.testing.framework.scenario;

import com.testlum.testing.model.scenario.Scenario;
import com.testlum.testing.report.ScenarioResult;

/**
 * Builds results for scenarios that were not executed, so reporting can show them as skipped with the reason.
 */
public final class SkippedScenarioResults {

    private SkippedScenarioResults() {
    }

    /**
     * @param args  arguments of a scenario that was not run (e.g. one of its integrations is disabled)
     * @param cause why the scenario was not run
     * @return a skipped result carrying the scenario's identity and run axes
     */
    public static ScenarioResult of(final ScenarioArguments args, final String cause) {
        ScenarioResult result = skipped(args.getFile().getPath(), cause);
        Scenario scenario = args.getScenario();
        if (scenario != null) {
            result.setOverview(scenario.getOverview());
            result.setName(scenario.getOverview() == null ? result.getName() : scenario.getOverview().getName());
            result.setTags(scenario.getSettings() == null ? null : scenario.getSettings().getTags());
        }
        result.setBrowser(args.getBrowser());
        result.setMobilebrowserDevice(args.getMobileBrowserDevice());
        result.setNativeDevice(args.getNativeDevice());
        result.setEnvironment(args.getEnvironment());
        result.setVariation(args.getVariations());
        return result;
    }

    /**
     * @param path  path of a scenario file that could not be run (e.g. it failed validation)
     * @param cause why the scenario was not run
     * @return a skipped result identified by the scenario path only
     */
    public static ScenarioResult of(final String path, final String cause) {
        return skipped(path, cause);
    }

    private static ScenarioResult skipped(final String path, final String cause) {
        ScenarioResult result = new ScenarioResult();
        result.setId(ScenarioRunner.nextScenarioId());
        result.setPath(path);
        result.setName(path);
        result.setSkipped(true);
        result.setCause(cause);
        result.setStartedAt(System.currentTimeMillis());
        return result;
    }
}

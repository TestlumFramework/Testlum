package com.testlum.testing.framework.scenario;

import com.testlum.testing.model.scenario.Scenario;
import com.testlum.testing.report.ScenarioResult;

/**
 * Builds results for scenarios that were not executed, so reporting can show them with the reason: as skipped
 * when the scenario was deliberately left out, as failed when it was expected to run but could not.
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
        setScenarioDetails(args, result);
        result.setBrowser(args.getBrowser());
        result.setMobilebrowserDevice(args.getMobileBrowserDevice());
        result.setNativeDevice(args.getNativeDevice());
        result.setEnvironment(args.getEnvironment());
        result.setVariation(args.getVariations());
        return result;
    }

    /**
     * An invalid scenario is a misconfiguration (e.g. it names an integration that does not exist), not a
     * deliberate exclusion, so it is reported as failed: hiding it among the skipped would let the launch pass.
     *
     * @param path  path of a scenario file that was expected to run but could not (e.g. it failed validation)
     * @param cause why the scenario could not run
     * @return a failed result identified by the scenario path only
     */
    public static ScenarioResult invalid(final String path, final String cause) {
        return notRun(path, cause, false);
    }

    private static ScenarioResult skipped(final String path, final String cause) {
        return notRun(path, cause, true);
    }

    private static ScenarioResult notRun(final String path, final String cause, final boolean skipped) {
        ScenarioResult result = new ScenarioResult();
        result.setId(ScenarioRunner.nextScenarioId());
        result.setPath(path);
        result.setName(path);
        result.setSkipped(skipped);
        result.setCause(cause);
        result.setStartedAt(System.currentTimeMillis());
        return result;
    }

    private static void setScenarioDetails(final ScenarioArguments args, final ScenarioResult result) {
        Scenario scenario = args.getScenario();
        if (scenario != null) {
            result.setOverview(scenario.getOverview());
            result.setName(scenario.getOverview() == null ? result.getName() : scenario.getOverview().getName());
            result.setTags(scenario.getSettings() == null ? null : scenario.getSettings().getTags());
            result.setVariationsFile(scenario.getSettings() == null ? null : scenario.getSettings().getVariations());
        }
    }
}

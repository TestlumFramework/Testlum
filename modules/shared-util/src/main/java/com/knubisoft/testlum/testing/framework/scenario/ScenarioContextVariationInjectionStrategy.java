package com.knubisoft.testlum.testing.framework.scenario;

/**
 * Strategy interface for injecting CSV variation values into a scenario step.
 *
 * <p>Each implementation handles a specific type of scenario step or placeholder format.
 * Strategies are evaluated in order by {@link ScenarioContext}, and every applicable
 * strategy is executed sequentially on the same step — the output of one strategy becomes
 * the input of the next.</p>
 *
 * <h2>Registered strategies (in evaluation order)</h2>
 * <ol>
 *   <li>{@link JsonVariationInjectionStrategy} — handles POST/PUT/PATCH steps whose
 *       {@code body.raw} field contains {@code {{...}}} placeholders. Injects values via
 *       Jackson tree manipulation, respecting parent markers ({@code $null}, {@code $absent},
 *       {@code $empty}, {@code $exists}).</li>
 *   <li>{@link DefaultVariationInjectionStrategy} — handles any remaining {@code {{...}}}
 *       placeholders in the step (e.g. in headers, endpoint, or non-body steps) via
 *       plain string substitution.</li>
 * </ol>
 *
 * <h2>Execution contract</h2>
 * <ul>
 *   <li>{@link #isApplicable} is always called before {@link #injectVariationsValues}.
 *       Implementations may cache state during the applicability check for reuse during injection
 *       (see {@link JsonVariationInjectionStrategy#isApplicable}).</li>
 *   <li>If {@link #isApplicable} returns {@code false}, {@link #injectVariationsValues}
 *       will not be called for that step.</li>
 *   <li>Implementations must not mutate the original {@link ScenarioContext}.</li>
 * </ul>
 */
public interface ScenarioContextVariationInjectionStrategy {

    /**
     * Returns {@code true} if this strategy can process the given scenario step.
     *
     * <p>Implementations may inspect the step structure or content to determine applicability.
     * A side effect of caching parsed state for later use in {@link #injectVariationsValues}
     * is acceptable, provided the state is scoped to a single step invocation.</p>
     *
     * @param scenarioStepAsString the scenario step serialized as a JSON string
     * @return {@code true} if this strategy should handle the step
     */
    boolean isApplicable(String scenarioStepAsString);


    String injectVariationsValues(String scenarioStepAsString,
                                  ScenarioContext scenarioContext,
                                  boolean escapeSpelQuotes);


    default String escapeSpelQuotes(final String value) {
        return value.replaceAll("'", "''");
    }

}
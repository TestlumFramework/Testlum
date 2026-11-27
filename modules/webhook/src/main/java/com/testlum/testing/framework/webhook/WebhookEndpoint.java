package com.testlum.testing.framework.webhook;

import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.util.WebhookUrlUtil;
import lombok.Getter;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A path on the inbound HTTP server that receives webhooks, built from a webhook integration.
 */
@Getter
public final class WebhookEndpoint {

    private static final Pattern PATH_VARIABLE = Pattern.compile("\\{([^/{}]+)}");
    private static final String ANY_SEGMENT = "[^/]+";

    private final String path;
    private final int responseStatusCode;
    private final Duration holdTimeout;
    private final WebhookMatcherType matcherType;
    private final String matcherKey;

    /**
     * Path with variable names erased.
     * <p>
     * For example, /orders/{} - two endpoints with the same key are the same route.
     */
    private final String routingKey;
    private final Set<String> pathVariables;
    private final Pattern pathPattern;

    public WebhookEndpoint(final String url,
                           final int responseStatusCode,
                           final Duration holdTimeout,
                           final WebhookMatcherType matcherType,
                           final String matcherKey) {
        this.path = WebhookUrlUtil.pathOf(url);
        this.responseStatusCode = responseStatusCode;
        this.holdTimeout = holdTimeout == null || holdTimeout.isNegative() ? Duration.ZERO : holdTimeout;
        this.matcherType = matcherType == null ? WebhookMatcherType.NONE : matcherType;
        this.matcherKey = matcherKey;
        this.pathVariables = findPathVariables(path);
        this.routingKey = PATH_VARIABLE.matcher(path).replaceAll("{}");
        this.pathPattern = pathVariables.isEmpty() ? null : Pattern.compile(toRegex(null, null));
        if (this.matcherType == WebhookMatcherType.PATH_VAR && !pathVariables.contains(matcherKey)) {
            throw cannotMatchPathException(matcherKey);
        }
    }

    private DefaultFrameworkException cannotMatchPathException(final String matcherKey) {
        return new DefaultFrameworkException(
                String.format("Webhook matcher byPathVar '%s' is not a {variable} in the integration url path '%s'",
                        matcherKey, path));
    }

    public boolean matchesPath(final String requestPath) {
        String normalized = WebhookUrlUtil.normalizePath(requestPath);
        return pathPattern == null ? path.equals(normalized) : pathPattern.matcher(normalized).matches();
    }

    public boolean hasPathVariables() {
        return pathPattern != null;
    }

    /**
     * Pattern of this path where #variable has to be equal to #value.
     */
    public Pattern pathPatternWith(final String variable, final String value) {
        return Pattern.compile(toRegex(variable, value));
    }

    /**
     * Whether both endpoints can share one route on the server without changing behavior.
     */
    public boolean sameSettingsAs(final WebhookEndpoint other) {
        return path.equals(other.path)
                && responseStatusCode == other.responseStatusCode
                && holdTimeout.equals(other.holdTimeout)
                && matcherType == other.matcherType
                && Objects.equals(matcherKey, other.matcherKey);
    }

    @Override
    public String toString() {
        return path;
    }

    private String toRegex(final String boundVariable, final String boundValue) {
        StringBuilder regex = new StringBuilder();
        Matcher matcher = PATH_VARIABLE.matcher(path);
        int literalStart = 0;
        while (matcher.find()) {
            regex.append(Pattern.quote(path.substring(literalStart, matcher.start())));
            boolean bound = matcher.group(1).equals(boundVariable);
            regex.append(bound ? Pattern.quote(boundValue) : ANY_SEGMENT);
            literalStart = matcher.end();
        }
        regex.append(Pattern.quote(path.substring(literalStart)));
        return regex.toString();
    }

    private static Set<String> findPathVariables(final String path) {
        Set<String> variables = new LinkedHashSet<>();
        Matcher matcher = PATH_VARIABLE.matcher(path);
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
        return variables;
    }

}

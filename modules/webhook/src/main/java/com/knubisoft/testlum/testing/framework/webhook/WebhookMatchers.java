package com.knubisoft.testlum.testing.framework.webhook;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.webhook.matcher.AlwaysMatchStrategy;
import com.knubisoft.testlum.testing.framework.webhook.matcher.HeaderMatchStrategy;
import com.knubisoft.testlum.testing.framework.webhook.matcher.JsonPathMatchStrategy;
import com.knubisoft.testlum.testing.framework.webhook.matcher.PathVariableMatchStrategy;
import com.knubisoft.testlum.testing.framework.webhook.matcher.QueryParamMatchStrategy;
import com.knubisoft.testlum.testing.framework.webhook.matcher.WebhookMatchStrategy;
import org.apache.commons.lang3.StringUtils;


/**
 * Builds the matcher of a <webhook> step from its integration's matcher and the step's matchValue.
 */
public final class WebhookMatchers {

    private WebhookMatchers() {
    }

    public static WebhookMatchStrategy forStep(final String alias, final WebhookEndpoint endpoint,
                                               final String matchValue) {
        WebhookMatcherType type = endpoint.getMatcherType();
        if (type == WebhookMatcherType.NONE) {
            return new AlwaysMatchStrategy();
        }
        if (StringUtils.isBlank(matchValue)) {
            throw noMatchValueException(alias, endpoint, type);
        }
        String key = endpoint.getMatcherKey();
        return switch (type) {
            case HEADER -> new HeaderMatchStrategy(key, matchValue, false);
            case QUERY_PARAM -> new QueryParamMatchStrategy(key, matchValue, false);
            case BODY_KEY -> new JsonPathMatchStrategy(key, matchValue);
            case PATH_VAR -> new PathVariableMatchStrategy(endpoint, key, matchValue);
            default -> throw new DefaultFrameworkException("Unsupported webhook matcher: " + type);
        };
    }

    private static DefaultFrameworkException noMatchValueException(final String alias,
                                                                   final WebhookEndpoint endpoint,
                                                                   final WebhookMatcherType type) {
        return new DefaultFrameworkException(String.format(
                "Webhook integration '%s' matches webhooks by %s '%s', so the <webhook> step needs matchValue",
                        alias, describe(type), endpoint.getMatcherKey()));
    }

    public static String describe(final WebhookMatcherType type) {
        return switch (type) {
            case HEADER -> "header";
            case QUERY_PARAM -> "query param";
            case BODY_KEY -> "body key";
            case PATH_VAR -> "path variable";
            default -> "nothing";
        };
    }


}

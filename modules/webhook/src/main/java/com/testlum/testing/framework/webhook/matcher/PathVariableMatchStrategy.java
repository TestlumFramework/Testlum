package com.testlum.testing.framework.webhook.matcher;

import com.testlum.testing.framework.inbound.server.dto.InboundRequest;
import com.testlum.testing.framework.util.WebhookUrlUtil;
import com.testlum.testing.framework.webhook.WebhookEndpoint;

import java.util.regex.Pattern;

public class PathVariableMatchStrategy implements WebhookMatchStrategy {

    private final WebhookEndpoint endpoint;
    private final String variable;
    private final String value;

    public PathVariableMatchStrategy(final WebhookEndpoint endpoint, final String variable, final String value) {
        this.endpoint = endpoint;
        this.variable = variable;
        this.value = value;
    }

    @Override
    public boolean matches(InboundRequest request) {
        Pattern pattern = endpoint.pathPatternWith(variable, value);
        return pattern.matcher(WebhookUrlUtil.normalizePath(request.getPath())).matches();
    }
}

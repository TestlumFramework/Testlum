package com.knubisoft.testlum.testing.framework.coordinator;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.inbound.server.util.CorrelationMatcher;
import com.knubisoft.testlum.testing.model.scenario.Webhook;
import com.knubisoft.testlum.testing.model.scenario.WebhookMatcher;
import com.knubisoft.testlum.testing.model.scenario.WebhookMatcherEndpoint;
import com.knubisoft.testlum.testing.model.scenario.WebhookMatcherJson;
import com.knubisoft.testlum.testing.model.scenario.WebhookMatcherNameValue;

import java.util.ArrayList;
import java.util.List;

public class WebhookMatcherBuilder {

    public static CorrelationMatcher buildMatcher(final Webhook webhook) {
        WebhookMatcher matcherStub = webhook.getWebhookMatcher();
        if (matcherStub == null) {
            return CorrelationMatcher.always();
        }
        List<WebhookMatcherNameValue> headers = nullSafeList(matcherStub.getByHeader());
        List<WebhookMatcherJson> jsons = nullSafeList(matcherStub.getByJson());
        List<WebhookMatcherNameValue> qParams = nullSafeList(matcherStub.getByQueryParam());
        List<WebhookMatcherEndpoint> paths = nullSafeList(matcherStub.getByEndpoint());
        boolean always = matcherStub.isAlways();
        int otherCount = headers.size() + jsons.size() + qParams.size() + paths.size();
        if (otherCount > 0 && always) {
            throw new DefaultFrameworkException("<always>=\"true\" cannot be combined with other criteria");
        }
        if (otherCount == 0 || always) {
            return CorrelationMatcher.always();
        }
        List<CorrelationMatcher> matchers = new ArrayList<>(otherCount);
        for (WebhookMatcherNameValue header : headers) {
            matchers.add(CorrelationMatcher.header(header.getName(), header.getValue(), false));
        }
        for (WebhookMatcherJson json : jsons) {
            matchers.add(CorrelationMatcher.jsonPath(json.getJpath(), json.getValue()));
        }
        for (WebhookMatcherNameValue qParam : qParams) {
            matchers.add(CorrelationMatcher.queryParam(qParam.getName(), qParam.getValue(), false));
        }
        for (WebhookMatcherEndpoint path : paths) {
            matchers.add(CorrelationMatcher.endpoint(path.getUrl()));
        }
        return CorrelationMatcher.combined(matchers);
    }

    private static <T> List<T> nullSafeList(final List<T> list) {
        return list != null ? list : List.of();
    }


}

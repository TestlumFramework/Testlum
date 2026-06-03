package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testing.framework.session.SessionEvent;
import com.knubisoft.testlum.testing.model.scenario.Webhook;
import com.knubisoft.testlum.testing.model.scenario.WebhookReceive;

import java.util.List;

public class WebhookInterpreterUtil {

    public static String failureMessage(final WebhookReceive receive,
                                        final Webhook webhook,
                                        final SessionEvent.Failed f) {
        return switch (f.status()) {
            case TIMEOUT ->
                    "No webhook received on '" + receive.getAlias() + "' within " + webhook.getTimeout() + " seconds";
            case BELOW_MIN_RETRIES -> "Webhook retry policy failed: " + f.errorMessage();
            case UNEXPECTED_EXTRA_RETRY -> "Unexpected extra webhook call after completion: " + f.extraIfPresent();
            default -> "Webhook handling failed: " + f.errorMessage();
        };
    }

}

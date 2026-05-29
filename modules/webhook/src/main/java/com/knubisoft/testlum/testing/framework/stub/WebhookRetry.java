package com.knubisoft.testlum.testing.framework.stub;

import java.util.List;

public class WebhookRetry {

    protected Integer min;
    protected Integer max;
    protected Integer idleTimeout;
    protected WebhookReceive receive;
    protected WebhookResponse response;

    protected List<WebhookRetryAttempt> attempts;

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public Integer getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(Integer idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public WebhookReceive getReceive() {
        return receive;
    }

    public void setReceive(WebhookReceive receive) {
        this.receive = receive;
    }

    public WebhookResponse getResponse() {
        return response;
    }

    public void setResponse(WebhookResponse response) {
        this.response = response;
    }

    public List<WebhookRetryAttempt> getAttempts() {
        return attempts;
    }

    public void setAttempts(List<WebhookRetryAttempt> attempts) {
        this.attempts = attempts;
    }
}

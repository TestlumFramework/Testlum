package com.knubisoft.testlum.testing.framework.stub;

public class WebhookRetry {

    protected Integer min;
    protected Integer max;
    protected String idleTimeout;
    protected WebhookReceive receive;
    protected WebhookResponse response;

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

    public String getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(String idleTimeout) {
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
}

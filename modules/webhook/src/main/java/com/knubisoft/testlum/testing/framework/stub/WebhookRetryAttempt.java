package com.knubisoft.testlum.testing.framework.stub;

public class WebhookRetryAttempt {

    protected WebhookReceive receive;
    protected WebhookResponse response;

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

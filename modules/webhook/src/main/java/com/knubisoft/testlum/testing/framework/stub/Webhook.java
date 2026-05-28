package com.knubisoft.testlum.testing.framework.stub;

import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;

public class Webhook extends AbstractCommand {

    protected WebhookReceive receive;

    protected WebhookResponse response;
    protected Integer timeout;

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

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
}

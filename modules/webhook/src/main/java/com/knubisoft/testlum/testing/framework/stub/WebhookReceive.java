package com.knubisoft.testlum.testing.framework.stub;

public class WebhookReceive {


    protected String expected;
    protected Boolean forward;
    protected String alias;

    public String getExpected() {
        return expected;
    }

    public void setExpected(String expected) {
        this.expected = expected;
    }

    public Boolean getForward() {
        return forward;
    }

    public void setForward(Boolean forward) {
        this.forward = forward;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}

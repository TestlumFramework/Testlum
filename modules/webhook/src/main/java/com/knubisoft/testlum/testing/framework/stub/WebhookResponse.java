package com.knubisoft.testlum.testing.framework.stub;

public class WebhookResponse {

    protected WebhookResponseExpected expected;

    protected WebhookMockResult mockResult;

    public WebhookResponseExpected getExpected() {
        return expected;
    }

    public void setExpected(WebhookResponseExpected expected) {
        this.expected = expected;
    }

    public WebhookMockResult getMockResult() {
        return mockResult;
    }

    public void setMockResult(WebhookMockResult mockResult) {
        this.mockResult = mockResult;
    }

}

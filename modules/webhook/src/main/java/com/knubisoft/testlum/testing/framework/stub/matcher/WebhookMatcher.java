package com.knubisoft.testlum.testing.framework.stub.matcher;

import java.util.List;

public class WebhookMatcher {

    private List<ByHeader> byHeader;
    private List<ByJson> byJson;
    private List<ByQueryParam> byQueryParam;
    private List<ByPath> byPath;
    private Boolean always;

    public List<ByHeader> getByHeader() {
        return byHeader;
    }

    public void setByHeader(List<ByHeader> byHeader) {
        this.byHeader = byHeader;
    }

    public List<ByJson> getByJson() {
        return byJson;
    }

    public void setByJson(List<ByJson> byJson) {
        this.byJson = byJson;
    }

    public List<ByQueryParam> getByQueryParam() {
        return byQueryParam;
    }

    public void setByQueryParam(List<ByQueryParam> byQueryParam) {
        this.byQueryParam = byQueryParam;
    }

    public List<ByPath> getByPath() {
        return byPath;
    }

    public void setByPath(List<ByPath> byPath) {
        this.byPath = byPath;
    }

    public Boolean getAlways() {
        return always;
    }

    public void setAlways(Boolean always) {
        this.always = always;
    }
}

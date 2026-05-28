package com.knubisoft.testlum.testing.framework.stub;

import com.knubisoft.testlum.testing.model.scenario.Header;

import java.util.List;

public class WebhookMockResult {

    protected Integer status;

    protected String file;
    protected List<Header> header;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public List<Header> getHeader() {
        return header;
    }

    public void setHeader(List<Header> header) {
        this.header = header;
    }
}

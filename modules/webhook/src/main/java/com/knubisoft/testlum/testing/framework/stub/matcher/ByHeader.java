package com.knubisoft.testlum.testing.framework.stub.matcher;

public class ByHeader {

    private String name;
    private String value;
    private Boolean contains;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean getContains() {
        return contains;
    }

    public void setContains(Boolean contains) {
        this.contains = contains;
    }
}

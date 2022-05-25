package com.knubisoft.e2e.testing.model.scenario;

public enum OverviewPart {
    NAME("Name"),
    DESCRIPTION("Description"),
    JIRA("Jira"),
    DEVELOPER("Developer");

    private final String partTitle;

    OverviewPart(final String partTitle) {
        this.partTitle = partTitle;
    }

    public String getPartTitle() {
        return partTitle;
    }
}

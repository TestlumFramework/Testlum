package com.knubisoft.e2e.testing.framework.report;

import lombok.Data;

@Data
public class AggregatedReport {

    private String projectName;
    private boolean allTestsPassed;
    private String branch;
    private String reportDate;
    private long totalScenarios;
    private long totalSuccessScenarios;
    private long totalFailedScenarios;
    private int coverage;

}

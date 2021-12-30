package com.knubisoft.e2e.testing.framework.report;

import com.knubisoft.e2e.testing.model.global_config.GlobalTestConfiguration;
import lombok.Data;

import java.util.List;

@Data
public class ReportResult {

    private GlobalTestConfiguration globalTestConfiguration;
    private AggregatedReport aggregatedReport;
    private List<ScenarioResult> scenarios;
}

package com.knubisoft.e2e.testing.framework.report;

import com.knubisoft.e2e.testing.framework.report.extentreports.ExtentReportsGenerator;
import com.knubisoft.e2e.testing.model.global_config.Report;
import lombok.experimental.UtilityClass;

import java.util.Objects;

@UtilityClass
public class ReportGeneratorFactory {

    public ReportGenerator create(Report report) {
        if (Objects.nonNull(report.getExtentReports())) {
            return new ExtentReportsGenerator();
        }
        throw new UnsupportedOperationException("Report generator type is not supported");
    }
}

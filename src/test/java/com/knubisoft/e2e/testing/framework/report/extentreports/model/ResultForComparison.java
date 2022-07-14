package com.knubisoft.e2e.testing.framework.report.extentreports.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ResultForComparison {

    private final String expected;
    private final String actual;
}

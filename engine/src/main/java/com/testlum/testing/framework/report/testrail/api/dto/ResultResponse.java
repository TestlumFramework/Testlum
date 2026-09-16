package com.testlum.testing.framework.report.testrail.api.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ResultResponse {
    private Integer id;
    private Integer testId;
    private Integer statusId;
}

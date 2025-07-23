package com.knubisoft.testlum.testing.framework.testRail.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
public class ResultResponseDto {
    private Integer id;
    private Integer testId;
    private Integer statusId;
}

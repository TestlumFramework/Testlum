package com.knubisoft.testlum.testing.framework.report.testrails.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResultRequestDto(
        @JsonProperty("case_id") String caseId,
        @JsonProperty("status_id") int statusId,
        @JsonProperty("comment") String comment,
        @JsonProperty("elapsed") String elapsed
) { }

package com.testlum.testing.framework.report.testrail.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResultRequest(@JsonProperty("case_id") String caseId,
                            @JsonProperty("status_id") int statusId,
                            @JsonProperty("comment") String comment,
                            @JsonProperty("elapsed") String elapsed) { }

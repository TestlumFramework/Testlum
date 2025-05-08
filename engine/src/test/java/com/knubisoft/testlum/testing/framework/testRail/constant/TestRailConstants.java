package com.knubisoft.testlum.testing.framework.testRail.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class TestRailConstants {

    public static final String ADD_RESULTS_FOR_CASES = "add_results_for_cases/";

    public static final String CASE_ID = "case_id";
    public static final String STATUS_ID = "status_id";
    public static final String COMMENT = "comment";
    public static final String RESULTS = "results";

    public static final int STATUS_PASSED = 1;
    public static final int STATUS_FAILED = 5;

    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String AUTH_BASIC_PREFIX = "Basic ";

    public static final String LOG_SENDING_RESULTS = "Sending results to TestRail Run ID: {}, cases: {}";
    public static final String LOG_SUCCESS_RESPONSE = "TestRail response for Run ID {}: {}";
    public static final String LOG_ERROR_RESPONSE = "Failed to send results to TestRail (Run ID: {}). Error: {}";

    public static final String COMMENT_PASSED_TEMPLATE = "Test case '%s' passed successfully.";
    public static final String COMMENT_FAILED_TEMPLATE = "Failure: %s";
}

package com.knubisoft.testlum.testing.framework.testRail.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class TestRailConstants {

    public static final String ADD_RESULTS_FOR_CASES_URL = "add_results_for_cases/";
    public static final String CREATE_NEW_TEST_RUN_URL = "add_run/";

    public static final String CASE_ID = "case_id";
    public static final String STATUS_ID = "status_id";
    public static final String COMMENT = "comment";
    public static final String RESULTS = "results";
    public static final String ID = "id";

    public static final String RUN_NAME = "name";
    public static final String RUN_DESCRIPTION = "description";
    public static final String RUN_INCLUDE_ALL = "include_all";
    public static final String RUN_CASE_IDS = "case_ids";

    public static final int STATUS_PASSED = 1;
    public static final int STATUS_FAILED = 5;

    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String AUTH_BASIC_PREFIX = "Basic ";

    public static final String LOG_SENDING_RESULTS = "Sending results to TestRail Run ID: {}, cases: {}";
    public static final String LOG_SUCCESS_RESPONSE = "TestRail response for Run ID {}: {}";
    public static final String LOG_ERROR_RESPONSE = "Failed to send results to TestRail (Run ID: {}). Error: {}";

    public static final String LOG_CREATING_TEST_RUN = "Creating Test Run: '{}', number of cases: {}";
    public static final String LOG_TEST_RUN_CREATED = "Test Run '{}' created successfully with ID {}";
    public static final String LOG_TEST_RUN_CREATION_FAILED = "Failed to create Test Run '{}': {}";

    public static final String COMMENT_PASSED_TEMPLATE = "Test case '%s' passed successfully.";
    public static final String COMMENT_FAILED_TEMPLATE = "Test case '%s' failed.\nFailure: %s";

    public static final String CASE_ID_ERROR_RESPONSE = "Failed to parse Case ID '{}' for TestRail.";
    public static final String RUN_ID_ERROR_RESPONSE = "Failed to parse Run ID '{}' for TestRail.";
}

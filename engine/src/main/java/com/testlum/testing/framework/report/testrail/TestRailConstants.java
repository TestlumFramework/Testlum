package com.testlum.testing.framework.report.testrail;

public final class TestRailConstants {

    public static final String ID_FIELD = "id";
    public static final String CASE_ID = "case_id";
    public static final String TEST_ID = "test_id";
    public static final String STATUS_ID = "status_id";
    public static final String RESULTS = "results";

    public static final int STATUS_PASSED = 1;
    public static final int STATUS_FAILED = 5;

    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String AUTH_BASIC_PREFIX = "Basic ";

    public static final String LOG_SENDING_RESULTS = "Sending results to TestRail Run ID: {}, cases: {}";
    public static final String LOG_SUCCESS_RESPONSE = "TestRail response for Run ID {}: {}";
    public static final String LOG_ERROR_RESPONSE = "Failed to send results to TestRail (Run ID: {}). Error: {}";

    public static final String LOG_VALIDATE_CONNECTION = "Validating TestRail connection: {}";
    public static final String LOG_CONNECTION_SUCCESSFUL =
            "TestRail connection and authentication validated successfully";
    public static final String LOG_CONNECTION_FAILED = "TestRail connection failed: {}";
    public static final String LOG_CONNECTION_ERROR = "Error validating TestRail connection: {}";
    public static final String LOG_CREATING_TEST_RUN = "Creating Test Run: '{}', number of cases: {}";
    public static final String LOG_TEST_RUN_CREATED = "Test Run '{}' created successfully with ID {}";
    public static final String LOG_TEST_RUN_CREATION_FAILED = "Failed to create Test Run '{}': {}";

    public static final String LOG_FETCHING_TEST = "Fetching test data for Test ID: {}";
    public static final String LOG_FETCHING_TEST_SUCCESS_RESPONSE =
            "TestRail response for fetching test data with Test ID {}: {}";
    public static final String LOG_FETCHING_TEST_ERROR_RESPONSE =
            "Failed to get test data for Test ID: {}. Error: {}";

    public static final String ATTACHMENT_KEY = "attachment";
    public static final String ATTACHMENT_DEFAULT_FILENAME = "error_screenshot.jpg";
    public static final String LOG_SENDING_ATTACHMENT = "Sending attachment to TestRail Result ID: {}";
    public static final String LOG_ATTACHMENT_SUCCESS_RESPONSE =
            "TestRail response for attachment for Result ID {}: {}";
    public static final String LOG_ATTACHMENT_ERROR_RESPONSE =
            "Failed to send screenshot to TestRail (Result ID: {}). Error: {}";

    public static final String COMMENT_PASSED_TEMPLATE = "Test case '%s' passed successfully.";
    public static final String COMMENT_FAILED_TEMPLATE = "Test case '%s' failed.\nFailure: %s";

    public static final String CASES_FIELD = "cases";
    public static final String CASES_PAGE_LIMIT = "250";

    public static final String LOG_FETCHING_CASES_FAILED =
            "Failed to fetch TestRail cases for project {}. Error: {}";
    public static final String LOG_CASES_FETCHED =
            "Fetched {} TestRail cases carrying a value for custom key '{}'";

    public static final String CASE_MATCH_KEY_NOT_CONFIGURED =
            "Scenario '{}' uses caseMatchKeyValue but <caseMatchKey> is not set in "
                    + "<testRailReports> of the global config. Scenario is skipped.";
    public static final String CASE_MATCH_KEY_VALUE_NOT_FOUND =
            "No TestRail case has value '{}' for custom key '{}' (scenario '{}'). Scenario is skipped.";
    public static final String CASE_MATCH_KEY_VALUE_DUPLICATED =
            "Custom key '{}' has value '{}' on more than one TestRail case ({} and {}). Case {} is used.";
    public static final String TEST_RAIL_CASE_NOT_RESOLVED =
            "Scenario '{}' has neither a usable testCaseId nor a caseMatchKeyValue. Scenario is skipped.";

    public static final String RUN_ID_ERROR_RESPONSE = "Failed to parse Run ID '{}' for TestRail.";

    public static final String ID_FETCH_ERROR_RESPONSE = "Failed to obtain field '{}' from JSON response.";
}

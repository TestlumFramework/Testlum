package com.knubisoft.testlum.testing.framework.testRail.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class TestRailConstants {

    public static final String ADD_RESULTS_FOR_CASES_URL = "add_results_for_cases/";
    public static final String CREATE_NEW_TEST_RUN_URL = "add_run/";
    public static final String GET_TEST_URL = "get_test/";
    public static final String ADD_ATTACHMENT_TO_RESULT_URL = "add_attachment_to_result/";
	public static final String GET_PROJECT_URL = "get_project/";
	public static final String GET_SUITE_URL = "get_suite/";
	public static final String GET_RUN_URL = "get_run/";
	public static final String UPDATE_RUN_URL = "update_run/";
	public static final String GET_RUNS_URL = "get_runs/";

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
    public static final String LOG_UPDATING_TEST_RUN = "Updating Test Run: '{}', number of cases: {}";
    public static final String LOG_TEST_RUN_CREATED = "Test Run '{}' created successfully with ID {}";
    public static final String LOG_TEST_RUN_CREATION_FAILED = "Failed to create Test Run '{}': {}";
    public static final String LOG_TEST_RUN_UPDATING_FAILED = "Failed to update Test Run '{}': {}";
	public static final String LOG_GET_PROJECT = "Getting Project {} from test rail: '{}'";
	public static final String LOG_GET_PROJECT_FAILED = "Failed to get Project {} from test rail: '{}' : {}";
	public static final String LOG_GET_SUITE = "Getting Suite {} from test rail: '{}'";
	public static final String LOG_GET_SUITE_FAILED = "Failed to get Suite {} from test rail: '{}' : {}";
	public static final String LOG_GET_RUN = "Getting Run {} from test rail: '{}'";
	public static final String LOG_GET_RUN_FAILED = "Failed to get Run {} from test rail: '{}' : {}";
	public static final String LOG_GET_RUNS = "Getting Runs by project {} from test rail: '{}'";
	public static final String LOG_GET_RUNS_FAILED = "Failed to get Runs by project {} from test rail: '{}' : {}";

    public static final String LOG_FETCHING_TEST = "Fetching test data for Test ID: {}";
    public static final String LOG_FETCHING_TEST_SUCCESS_RESPONSE = "TestRail response for fetching test data with Test ID {}: {}";
    public static final String LOG_FETCHING_TEST_ERROR_RESPONSE = "Failed to get test data for Test ID: {}. Error: {}";

    public static final String ATTACHMENT_KEY = "attachment";
    public static final String ATTACHMENT_DEFAULT_FILENAME = "error_screenshot.jpg";
    public static final String LOG_SENDING_ATTACHMENT = "Sending attachment to TestRail Result ID: {}";
    public static final String LOG_ATTACHMENT_SUCCESS_RESPONSE = "TestRail response for attachment for Result ID {}: {}";
    public static final String LOG_ATTACHMENT_ERROR_RESPONSE = "Failed to send screenshot to TestRail (Result ID: {}). Error: {}";

    public static final String COMMENT_PASSED_TEMPLATE = "Test case '%s' passed successfully.";
    public static final String COMMENT_FAILED_TEMPLATE = "Test case '%s' failed.\nFailure: %s";

    public static final String CASE_ID_ERROR_RESPONSE = "Failed to parse Case ID '{}' for TestRail.";
    public static final String RUN_ID_ERROR_RESPONSE = "Failed to parse Run ID '{}' for TestRail.";

    public static final String ID_FETCH_ERROR_RESPONSE = "Failed to obtain field '{}' from JSON response.";
}

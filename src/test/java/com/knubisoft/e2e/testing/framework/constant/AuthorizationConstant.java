package com.knubisoft.e2e.testing.framework.constant;

public class AuthorizationConstant {
    public static final String LOGIN_FIRST_STEP_URL = "api/v1/login";
    public static final String LOGIN_SECOND_STEP_URL = "api/v1/login/otp";
    public static final String TWO_FA_TOKEN_TYPE = "EMAIL_2FA";
    public static final String TOKEN_JPATH = "$.body.token";
    public static final String EMAIL_JPATH = "$.email";
    public static final String CONTENT_KEY_TOKEN = "token";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_BEARER = "Bearer ";
    public static final String HEADER_BASIC = "Basic ";
    public static final String TENANT_NAME = "tenant_%s";
    public static final String CENTRAL_DB = "central";
    public static final String GET_TWO_FA_TOKEN_QUERY = "SELECT token FROM t_user_one_time_token "
            + "WHERE type='EMAIL_2FA'";
    public static final String GET_TENANT_ID_QUERY = "SELECT tenant_id FROM t_user_email_to_tenant WHERE email='%s'";
    public static final String LOGIN_SECOND_STEP_JSON = "{ \"email\":\"%s\", \"token\":\"%s\","
            + "    \"code\":\"%s\"%n"
            + "    \"type\":\"%s\"%n"
            + "}";
}

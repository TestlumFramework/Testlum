package com.knubisoft.testlum.testing.framework.webhook;

/**
 * Which part of an incoming webhook carries the value that
 * a <webhook matchValue="..."> step looks for.
 */
public enum WebhookMatcherType {

    /**
     * Any webhook on the endpoint matches.
     */
    NONE,

    /**
     * Value of the request header with the configured name.
     */
    HEADER,

    /**
     * Value of the query parameter with the configured name.
     */
    QUERY_PARAM,

    /**
     * Value at the configured key / JSON path of the request body.
     */
    BODY_KEY,

    /**
     * Value of the configured {path_variable} in the integration url path.
     */
    PATH_VAR

}

package com.knubisoft.cott.testing.framework.util;

import lombok.Getter;

@Getter
public class GraphqlUtil {
    private String query;

    public GraphqlUtil(final String query) {
        this.query = query;
    }
}

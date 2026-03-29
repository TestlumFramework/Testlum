package com.knubisoft.testlum.testing.framework.db.source;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link Source} interface verifying the query delimiter constant.
 */
class SourceTest {

    @Test
    void queryDelimiterIsSemicolonSemicolon() {
        assertEquals(";;", Source.QUERY_DELIMITER);
    }
}

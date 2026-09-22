package com.testlum.testing.framework.report.testrail.util;

import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.report.testrail.api.util.TestRailUrlFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestRailUrlFormatterTest {

    private static final String EXPECTED = "https://testlum.testrail.io/index.php?/api/v2/";

    @ParameterizedTest
    @ValueSource(strings = {
            "https://testlum.testrail.io",
            "https://testlum.testrail.io/",
            "https://testlum.testrail.io///",
            "  https://testlum.testrail.io  ",
            "testlum.testrail.io",
            "https://testlum.testrail.io/index.php",
            "https://testlum.testrail.io/index.php?",
            "https://testlum.testrail.io/index.php?/api/v2",
            "https://testlum.testrail.io/index.php?/api/v2/",
            "https://testlum.testrail.io/INDEX.PHP"
    })
    void anyFormOfInstanceUrlIsFormattedToApiUrl(final String url) {
        assertEquals(EXPECTED, TestRailUrlFormatter.format(url));
    }

    @ParameterizedTest
    @CsvSource({
            "http://localhost:8080/testrail, http://localhost:8080/testrail/index.php?/api/v2/",
            "http://localhost:8080/testrail/index.php?/api/v2, http://localhost:8080/testrail/index.php?/api/v2/",
            "https://testlum.testrail.io/tr/index.php, https://testlum.testrail.io/tr/index.php?/api/v2/"
    })
    void instanceHostedInSubDirectoryKeepsItsPath(final String url, final String expected) {
        assertEquals(expected, TestRailUrlFormatter.format(url));
    }

    @Test
    void urlAlreadyPointingToApiIsKeptAsItIs() {
        assertEquals(EXPECTED, TestRailUrlFormatter.format(EXPECTED));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "/", "///"})
    void blankUrlIsRejected(final String url) {
        assertThrows(DefaultFrameworkException.class, () -> TestRailUrlFormatter.format(url));
    }
}

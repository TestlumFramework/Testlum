package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.scenario.AssertEqual;
import com.knubisoft.testlum.testing.model.scenario.AssertEquality;
import com.knubisoft.testlum.testing.model.scenario.AssertNotEqual;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AssertEqualityHelper {

    private static final String ASSERT_CONTENT_NOT_EQUAL = "Equality content <%s> is not equal.";
    private static final String ASSERT_CONTENT_IS_EQUAL = "Inequality content <%s> is equal.";

    public static void checkContentIsEqual(final AssertEqual equal) {
        if (equal.getContent().stream()
                    .map(AssertEqualityHelper::normalizeLineEndings)
                    .distinct()
                    .count() != 1) {
            throw new DefaultFrameworkException(String.format(ASSERT_CONTENT_NOT_EQUAL, formatContent(equal)));
        }
    }

    public static void checkContentNotEqual(final AssertNotEqual notEqual) {
        List<String> content = notEqual.getContent();
        if (content.stream()
                    .map(AssertEqualityHelper::normalizeLineEndings)
                    .distinct()
                    .count() == 1) {
            throw new DefaultFrameworkException(String.format(ASSERT_CONTENT_IS_EQUAL, formatContent(notEqual)));
        }
    }

    public static String formatContent(final AssertEquality action) {
        return String.join(DelimiterConstant.COMMA, action.getContent());
    }

    private static String normalizeLineEndings(final String content) {
        if (content == null) {
            return null;
        }
        return content.lines()
                .collect(Collectors.joining("\n"));
    }
}

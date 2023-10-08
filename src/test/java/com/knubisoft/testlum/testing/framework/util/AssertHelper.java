package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.AssertEqual;
import com.knubisoft.testlum.testing.model.scenario.AssertEquality;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.CLOSE_SQUARE_BRACKET;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.COMMA;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.OPEN_SQUARE_BRACKET;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.SPACE;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.ASSERT_CONTENT_IS_EQUAL;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.ASSERT_CONTENT_NOT_EQUAL;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.CONTENT_LOG;
import static java.lang.String.format;

@Slf4j
@UtilityClass
public class AssertHelper {

    public void executeEqualityCommand(final AssertEquality assertEquality, final CommandResult result) {
        List<String> content = assertEquality.getContent();
        String formattedContent = formatContent(content);
        log.info(CONTENT_LOG, formattedContent);
        ResultUtil.addAssertEqualityMetaData(assertEquality, result);
        if (assertEquality instanceof AssertEqual) {
            if (content.stream().distinct().count() != 1) {
                throw new DefaultFrameworkException(format(ASSERT_CONTENT_NOT_EQUAL, formattedContent));
            }
        } else if (content.stream().distinct().count() == 1) {
            throw new DefaultFrameworkException(format(ASSERT_CONTENT_IS_EQUAL, formattedContent));
        }
    }

    private String formatContent(final List<String> content) {
        return content.stream()
                .map(str -> OPEN_SQUARE_BRACKET + str + CLOSE_SQUARE_BRACKET)
                .collect(Collectors.joining(COMMA + SPACE));
    }
}

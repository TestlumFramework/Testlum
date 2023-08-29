package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.AssertEqual;
import com.knubisoft.testlum.testing.model.scenario.AssertEquality;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.COMMA;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.ASSERT_CONTENT_IS_EQUAL;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.ASSERT_CONTENT_NOT_EQUAL;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.CONTENT_LOG;

@Slf4j
@UtilityClass
public class AssertHelper {

    public void executeEqualityCommand(final AssertEquality assertEquality, final CommandResult result) {
        List<String> content = assertEquality.getContent();
        log.info(CONTENT_LOG, formatContent(content));
        ResultUtil.addAssertEqualityMetaData(assertEquality, result);
        if (assertEquality instanceof AssertEqual) {
            if (content.stream().distinct().count() != 1) {
                throw new DefaultFrameworkException(String.format(ASSERT_CONTENT_NOT_EQUAL, formatContent(content)));
            }
        } else {
            if (content.stream().distinct().count() == 1) {
                throw new DefaultFrameworkException(String.format(ASSERT_CONTENT_IS_EQUAL, formatContent(content)));
            }
        }
    }

    private String formatContent(final List<String> content) {
        return String.join(COMMA, content);
    }
}

package com.knubisoft.testlum.testing.framework.db.source;

import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

public class ListSource implements Source {

    private final List<String> queries;

    public ListSource(final List<String> queries) {
        this.queries = queries.stream()
                .flatMap(s -> Arrays.stream(s.split(QUERY_DELIMITER)))
                .map(s -> s.replaceAll(StringUtils.LF, DelimiterConstant.SPACE))
                .filter(s -> StringUtils.isNotBlank(s.trim()))
                .toList();
    }

    @Override
    public List<String> getQueries() {
        return queries;
    }
}

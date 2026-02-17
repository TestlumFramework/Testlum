package com.knubisoft.testlum.testing.framework.db.source;

import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;

import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.LF;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class ListSource implements Source {

    private final List<String> queries;

    public ListSource(final List<String> queries) {
        this.queries = queries.stream()
                .flatMap(s -> Arrays.stream(s.split(QUERY_DELIMITER)))
                .map(s -> s.replaceAll(LF, DelimiterConstant.SPACE))
                .filter(s -> isNotBlank(s.trim()))
                .toList();
    }

    @Override
    public List<String> getQueries() {
        return queries;
    }
}

package com.knubisoft.e2e.testing.framework.db.source;

import com.knubisoft.e2e.testing.framework.constant.DelimiterConstant;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.LF;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class ListSource implements Source {

    private final List<String> queries;

    public ListSource(final List<String> queries) {
        this.queries = queries.stream()
                .map(s -> s.replaceAll(LF, DelimiterConstant.SPACE))
                .filter(s -> isNotEmpty(s.trim()))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getQueries() {
        return queries;
    }
}

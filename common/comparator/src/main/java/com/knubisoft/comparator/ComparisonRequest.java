package com.knubisoft.comparator;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.w3c.dom.Node;

@AllArgsConstructor
@Getter
public class ComparisonRequest {

    private final Mode mode;
    private final ComparisonValue expected;
    private final ComparisonValue actual;

    @AllArgsConstructor
    @Getter
    public static final class ComparisonValue {

        private final String value;
        private final JsonNode json;
        private final Node xml;

        public boolean isJson() {
            return json != null;
        }

        public boolean isXml() {
            return xml != null;
        }
    }
}

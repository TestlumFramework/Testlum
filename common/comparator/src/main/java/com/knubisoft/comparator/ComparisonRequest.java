package com.knubisoft.comparator;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.w3c.dom.Node;

@AllArgsConstructor
@Getter
public class ComparisonRequest {

    private Mode mode;
    private ComparisonValue expected;
    private ComparisonValue actual;

    @AllArgsConstructor
    @Getter
    public static final class ComparisonValue {

        private String value;
        private JsonNode json;
        private Node xml;

        public boolean isJson() {
            return json != null;
        }

        public boolean isXml() {
            return xml != null;
        }
    }
}

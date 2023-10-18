package com.knubisoft.testlum.testing.framework.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.EMPTY;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.OPEN_BRACE;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.OPEN_SQUARE_BRACKET;

@UtilityClass
public class StringPrettifier {

    private static final int CHAR_LIMIT_FOR_CUT = 150;

    public String prettify(final String string) {
        if (StringUtils.isNotBlank(string)) {
            return string.replaceAll("\\s+", EMPTY);
        }
        return string;
    }

    public String prettifyToSave(final String actual) {
        try {
            return tryToPrettify(actual);
        } catch (Exception ignore) {
            return actual;
        }
    }

    private String tryToPrettify(final String actual) {
        if (actual.startsWith(OPEN_BRACE) || actual.startsWith(OPEN_SQUARE_BRACKET)) {
            Object json = JacksonMapperUtil.readValue(actual, Object.class);
            return JacksonMapperUtil.writeValueAsStringWithDefaultPrettyPrinter(json);
        }
        return actual;
    }

    public String cut(final String actual) {
        if (StringUtils.isNotBlank(actual) && actual.length() > CHAR_LIMIT_FOR_CUT) {
            return StringUtils.abbreviate(actual, CHAR_LIMIT_FOR_CUT);
        }
        return actual;
    }

    //use this method only to add value to CommandResult
    public String asJsonResult(final String json) {
        if (StringUtils.isBlank(json)) {
            return EMPTY;
        }
        try {
            return getPrettyJson(json);
        } catch (JsonParseException ignore) {
            return json;
        }
    }

    private String getPrettyJson(final String json) {
        JsonElement je = JsonParser.parseString(json);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(je);
    }
}

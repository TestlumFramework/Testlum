package com.knubisoft.cott.testing.framework.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class PrettifyStringJson {

    //use this method only to add value to CommandResult
    public String getJSONResult(final String json) {
        if (StringUtils.isBlank(json)) {
            return DelimiterConstant.EMPTY;
        }
        try {
            return getPrettyJSON(json);
        } catch (JsonParseException ignore) {
            return json;
        }
    }

    private String getPrettyJSON(final String json) {
        JsonElement je = JsonParser.parseString(json);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(je);
    }
}

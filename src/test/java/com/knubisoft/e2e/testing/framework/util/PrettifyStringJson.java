package com.knubisoft.e2e.testing.framework.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class PrettifyStringJson {

    public String getJSONResult(final String json) {
        if (StringUtils.isEmpty(json)){
            return getPrettyJSON("null");
        }
        return getPrettyJSON(json);
    }

    private String getPrettyJSON(final String json) {
        JsonElement je = JsonParser.parseString(json);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(je);
    }
}

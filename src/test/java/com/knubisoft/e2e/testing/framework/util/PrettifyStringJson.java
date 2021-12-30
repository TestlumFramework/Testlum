package com.knubisoft.e2e.testing.framework.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PrettifyStringJson {

    public String getJSONResult(final String json) {
        JsonElement je = JsonParser.parseString(json);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(je);
    }

}

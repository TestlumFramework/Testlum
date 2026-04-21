package com.knubisoft.testlum.testing.framework.scenario.jsonInjection;

public class PlaceholderNormalizer {

    private static final String PLACEHOLDERS_WITHOUT_QUOTES_PATTERN = "(?<!\")\\{\\{([^}]+)}}(?!\")";
    private static final String RAW_STRING_WRAPPER = "\"__RAW__{{$1}}__RAW__\"";

    /**
     * //"__RAW__{{scoresMaxItems[]}}__RAW__" -> add such markers for placeholders without quotes
     *
     * @param currentRawJsonText passed json as String
     * @return replaced String with values if any
     */
    public String replacePlaceholdersWithoutQuotesWithRawMark(String currentRawJsonText) {
        return currentRawJsonText.replaceAll(PLACEHOLDERS_WITHOUT_QUOTES_PATTERN, RAW_STRING_WRAPPER);

    }

    public String denormalize(String scenarioPlaceholder, String ejectedJson, String valueForPlaceholder) {
        String[] csvArray = valueForPlaceholder.split(",");
        var rawPlaceholder = "__RAW__" + scenarioPlaceholder + "__RAW__";
        if (ejectedJson.contains(rawPlaceholder)) {
            return denormalizeRawContent(valueForPlaceholder, rawPlaceholder, ejectedJson);
        } else if (csvArray.length > 1) {
            valueForPlaceholder = valueForPlaceholder.substring(1, valueForPlaceholder.length() - 1);
        }
        ejectedJson = ejectedJson.replace(scenarioPlaceholder, valueForPlaceholder);

        return ejectedJson;
    }

    private String denormalizeRawContent(String valueForPlaceholder, String rawPlaceholder, String ejectedJson) {
        String[] split = valueForPlaceholder.split(",");
        boolean shouldCutOuterQuotes;
        if (split.length > 1) {
            shouldCutOuterQuotes = checkIfSanitizeNeeded(split[0]);
        } else {
            shouldCutOuterQuotes = checkIfSanitizeNeeded(valueForPlaceholder);
        }
        if (shouldCutOuterQuotes) {
            ejectedJson = ejectedJson.replace("\"" + rawPlaceholder + "\"", valueForPlaceholder);
        } else {
            ejectedJson = ejectedJson.replace(rawPlaceholder, valueForPlaceholder);
        }
        return ejectedJson;
    }

    private static boolean checkIfSanitizeNeeded(String valueForPlaceholder) {
        if (valueForPlaceholder == null || valueForPlaceholder.equals("null")) {
            return true;
        }
        if (valueForPlaceholder.equals("true") || valueForPlaceholder.equals("false")) {
            return true;
        }
        try {
            Double.parseDouble(valueForPlaceholder);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

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
    public String replacePlaceholdersWithoutQuotesWithRawMark(final String currentRawJsonText) {
        return currentRawJsonText.replaceAll(PLACEHOLDERS_WITHOUT_QUOTES_PATTERN, RAW_STRING_WRAPPER);

    }

    public String denormalize(final String scenarioPlaceholder,
                              final String ejectedJson,
                              final String valueForPlaceholder) {
        String[] csvArray = valueForPlaceholder.split(",");
        String valueForReplacement = valueForPlaceholder;
        String ejectedJsonNotFinal;
        String rawPlaceholder = "__RAW__" + scenarioPlaceholder + "__RAW__";
        if (ejectedJson.contains(rawPlaceholder)) {
            return denormalizeRawContent(valueForPlaceholder, rawPlaceholder, ejectedJson);
        } else if (csvArray.length > 1) {
            valueForReplacement = valueForPlaceholder.substring(1, valueForPlaceholder.length() - 1);
        }
        ejectedJsonNotFinal = ejectedJson.replace(scenarioPlaceholder, valueForReplacement);

        return ejectedJsonNotFinal;
    }

    private String denormalizeRawContent(final String valueForPlaceholder,
                                         final String rawPlaceholder,
                                         final String ejectedJson) {
        String[] split = valueForPlaceholder.split(",");
        String notFinalEjectedJson;
        boolean shouldCutOuterQuotes;
        if (split.length > 1) {
            shouldCutOuterQuotes = checkIfSanitizeNeeded(split[0]);
        } else {
            shouldCutOuterQuotes = checkIfSanitizeNeeded(valueForPlaceholder);
        }
        notFinalEjectedJson = getNotFinalEjectedJson(valueForPlaceholder,
                rawPlaceholder,
                ejectedJson,
                shouldCutOuterQuotes);
        return notFinalEjectedJson;
    }

    private static String getNotFinalEjectedJson(final String valueForPlaceholder,
                                                 final String rawPlaceholder,
                                                 final String ejectedJson,
                                                 final boolean shouldCutOuterQuotes) {
        String notFinalEjectedJson;
        if (shouldCutOuterQuotes) {
            notFinalEjectedJson = ejectedJson.replace("\"" + rawPlaceholder + "\"", valueForPlaceholder);
        } else {
            notFinalEjectedJson = ejectedJson.replace(rawPlaceholder, valueForPlaceholder);
        }
        return notFinalEjectedJson;
    }

    private static boolean checkIfSanitizeNeeded(final String valueForPlaceholder) {
        if (valueForPlaceholder == null || "null".equals(valueForPlaceholder)) {
            return true;
        }
        if ("true".equals(valueForPlaceholder) || "false".equals(valueForPlaceholder)) {
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

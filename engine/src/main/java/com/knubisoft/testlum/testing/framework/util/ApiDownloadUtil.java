package com.knubisoft.testlum.testing.framework.util;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class ApiDownloadUtil {

    private static final Map<String, String> TYPE_MAP = new HashMap<>();

    static {
        TYPE_MAP.put("application/pdf", ".pdf");
        TYPE_MAP.put("application/msword", ".doc");
        TYPE_MAP.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx");
        TYPE_MAP.put("application/vnd.ms-excel", ".xls");
        TYPE_MAP.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx");
        TYPE_MAP.put("application/vnd.ms-powerpoint", ".ppt");
        TYPE_MAP.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", ".pptx");

        TYPE_MAP.put("application/json", ".json");
        TYPE_MAP.put("application/xml", ".xml");
        TYPE_MAP.put("text/xml", ".xml");
        TYPE_MAP.put("text/plain", ".txt");
        TYPE_MAP.put("text/csv", ".csv");
        TYPE_MAP.put("text/html", ".html");

        TYPE_MAP.put("application/zip", ".zip");
        TYPE_MAP.put("application/x-zip-compressed", ".zip");
        TYPE_MAP.put("application/gzip", ".gz");
        TYPE_MAP.put("application/x-rar-compressed", ".rar");

        TYPE_MAP.put("image/jpeg", ".jpg");
        TYPE_MAP.put("image/png", ".png");
        TYPE_MAP.put("image/gif", ".gif");
        TYPE_MAP.put("image/svg+xml", ".svg");
        TYPE_MAP.put("image/webp", ".webp");

        TYPE_MAP.put("audio/mpeg", ".mp3");
        TYPE_MAP.put("video/mp4", ".mp4");

        TYPE_MAP.put("application/octet-stream", ".bin");
    }

    public static Path saveToScenarioDir(final File scenarioFile,
                                         final Map<String, String> responseHeaders,
                                         final byte[] fileContent,
                                         final String fallbackBaseName) {
        if (fileContent == null || fileContent.length == 0) {
            return null;
        }

        Path scenarioDirectory = resolveScenarioDir(scenarioFile);
        if (scenarioDirectory == null || !Files.isDirectory(scenarioDirectory)) {
            return null;
        }

        String fileName = extractFileNameFromContentDisposition(responseHeaders);

        if (fileName == null || fileName.isBlank()) {
            String baseFileName = (fallbackBaseName == null || fallbackBaseName.isBlank())
                    ? "api_download"
                    : fallbackBaseName;

            if (baseFileName.contains(".")) {
                fileName = baseFileName;
            } else {
                fileName = baseFileName + guessExtension(responseHeaders);
            }
        }

        Path targetFilePath = scenarioDirectory.resolve(fileName);

        try {
            Files.deleteIfExists(targetFilePath);
            Files.write(targetFilePath, fileContent);

            return targetFilePath;
        } catch (IOException ioException) {
            return null;
        }
    }

    public static Path resolveScenarioDir(final File scenarioFile) {
        if (scenarioFile == null) {
            return null;
        }
        File parentDirectory = scenarioFile.getParentFile();
        return parentDirectory == null ? null : parentDirectory.toPath().toAbsolutePath().normalize();
    }

    public static String extractFileNameFromContentDisposition(final Map<String, String> headers) {
        String contentDispositionHeader = getHeaderIgnoreCase(headers, "Content-Disposition");
        if (contentDispositionHeader == null) {
            return null;
        }

        String contentDispositionLowerCase = contentDispositionHeader.toLowerCase(Locale.ROOT);

        int filenameStarIndex = contentDispositionLowerCase.indexOf("filename*=");
        if (filenameStarIndex >= 0) {
            String rawValue = contentDispositionHeader.substring(filenameStarIndex + "filename*=".length()).trim();
            rawValue = stripQuotes(rawValue);

            int encodingMarkerIndex = rawValue.indexOf("''");
            if (encodingMarkerIndex >= 0 && encodingMarkerIndex + 2 < rawValue.length()) {
                String encodedFileName = rawValue.substring(encodingMarkerIndex + 2);
                try {
                    return URLDecoder.decode(encodedFileName, StandardCharsets.UTF_8);
                } catch (Exception ignored) { }
            }
        }

        int filenameIndex = contentDispositionLowerCase.indexOf("filename=");
        if (filenameIndex >= 0) {
            String rawValue = contentDispositionHeader.substring(filenameIndex + "filename=".length()).trim();
            rawValue = stripQuotes(rawValue);

            int semicolonIndex = rawValue.indexOf(';');
            if (semicolonIndex > 0) {
                rawValue = rawValue.substring(0, semicolonIndex).trim();
            }
            return rawValue.isBlank() ? null : rawValue;
        }

        return null;
    }

    public static String guessExtension(final Map<String, String> headers) {
        String contentTypeHeader = getHeaderIgnoreCase(headers, "Content-Type");
        if (contentTypeHeader == null || contentTypeHeader.isBlank()) {
            return ".bin";
        }

        String mimeType = contentTypeHeader.split(";")[0].trim().toLowerCase(Locale.ROOT);

        return TYPE_MAP.getOrDefault(mimeType, ".bin");
    }

    public static String getHeaderIgnoreCase(final Map<String, String> headers, final String targetKey) {
        if (headers == null || targetKey == null) {
            return null;
        }
        for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
            if (targetKey.equalsIgnoreCase(headerEntry.getKey())) {
                return headerEntry.getValue();
            }
        }
        return null;
    }

    private static String stripQuotes(String value) {
        if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
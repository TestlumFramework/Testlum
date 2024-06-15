package com.knubisoft.testlum.testing.framework.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.knubisoft.testlum.testing.framework.constant.StatisticsConstant.*;

public class UserValidator {

    private static final Logger log = LoggerFactory.getLogger(UserValidator.class);

    public static String validateUsername(final String username, final String errorMessage) {
        sendCheckUsernameRequest(username, errorMessage);
        return username;
    }

    private static void sendCheckUsernameRequest(final String username, final String errorMessage) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = buildCheckUsernameRequest(username);
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.error(String.format(errorMessage, username));
                throw new IllegalStateException(String.format(errorMessage, username));
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static HttpRequest buildCheckUsernameRequest(final String username) {
        return HttpRequest.newBuilder()
                .uri(URI.create(String.format(API_BASE_URL + USERNAME_CHECK_URL + "?userKey=%s", username)))
                .header(API_KEY_HEADER, API_KEY)
                .header("Content-Type", "application/json")
                .GET()
                .build();
    }

}

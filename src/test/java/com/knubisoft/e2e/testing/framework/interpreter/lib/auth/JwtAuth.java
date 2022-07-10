package com.knubisoft.e2e.testing.framework.interpreter.lib.auth;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.e2e.testing.framework.constant.DelimiterConstant;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.util.JacksonMapperUtil;
import com.knubisoft.e2e.testing.model.scenario.Auth;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Map;

@Slf4j
public class JwtAuth extends AbstractAuthStrategy {

    private static final String BEARER_PREFIX = "Bearer ";

    public JwtAuth(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void authenticate(final Auth auth, final CommandResult result) {
        Map<String, ? extends String> body = prepareBody(auth);
        String token = getJwtToken(body, auth);
        result.put("Auth-type", BEARER_PREFIX.replaceAll(DelimiterConstant.SPACE, DelimiterConstant.EMPTY));
        login(token, BEARER_PREFIX);
    }

    @SneakyThrows
    private String getJwtToken(final Map<String, ? extends String> loginRequest, final Auth auth) {
        String body = JacksonMapperUtil.writeValueAsString(loginRequest);
        HttpHeaders headers = getHeaders(body);
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForObject(getBaseApiUrl(auth) + auth.getLoginEndpoint(),
                request, String.class);
    }

    @SneakyThrows
    private Map<String, ? extends String> prepareBody(final Auth auth) {
        return JacksonMapperUtil.readValue(
                new File(TestResourceSettings.getInstance().getCredentialsFolder(), auth.getCredentials()),
                Map.class
        );
    }

    private HttpHeaders getHeaders(final String credentials) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(credentials);
        return headers;
    }

    private String getBaseApiUrl(final Auth auth) {
        return GlobalTestConfigurationProvider.provide().getIntegrations().getApis().getApi().stream()
                .filter(api -> api.getAlias().equalsIgnoreCase(auth.getApiAlias()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Alias doesn't exist."))
                .getUrl();
    }
}

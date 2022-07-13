package com.knubisoft.e2e.testing.framework.interpreter.lib.auth;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.constant.DelimiterConstant;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.util.AuthUtil;
import com.knubisoft.e2e.testing.model.scenario.Auth;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class JwtAuth extends AbstractAuthStrategy {

    private static final String BEARER_PREFIX = "Bearer ";
    private final InterpreterDependencies interpreterDependencies;

    public JwtAuth(final InterpreterDependencies dependencies) {
        super(dependencies);
        this.interpreterDependencies = dependencies;
    }

    @Override
    public void authenticate(final Auth auth, final CommandResult result) {
        String body = prepareBody(auth);
        String token = getJwtToken(body, auth);
        result.put("Auth-type", BEARER_PREFIX.replaceAll(DelimiterConstant.SPACE, DelimiterConstant.EMPTY));
        login(token, BEARER_PREFIX);
    }

    @SneakyThrows
    private String getJwtToken(final String body, final Auth auth) {
        HttpHeaders headers = getHeaders();
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForObject(getBaseApiUrl(auth) + auth.getLoginEndpoint(),
                request, String.class);
    }

    @SneakyThrows
    private String prepareBody(final Auth auth) {
        return AuthUtil.getCredentialsFromFile(
                interpreterDependencies.getFileSearcher(), auth.getCredentials()
        );
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
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

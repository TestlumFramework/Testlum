package com.knubisoft.cott.testing.framework.interpreter.lib.auth;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.constant.AuthorizationConstant;
import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.AuthUtil;
import com.knubisoft.cott.testing.framework.util.ConfigUtil;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.model.global_config.Api;
import com.knubisoft.cott.testing.model.scenario.Auth;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

import static com.knubisoft.cott.testing.framework.util.ResultUtil.AUTHENTICATION_TYPE;

@Slf4j
public class JwtAuth extends AbstractAuthStrategy {

    public JwtAuth(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void authenticate(final Auth auth, final CommandResult result) {
        LogUtil.logAuthInfo(auth);
        String token = getJwtToken(auth);
        result.put(AUTHENTICATION_TYPE, AuthorizationConstant.HEADER_JWT);
        login(token, AuthorizationConstant.HEADER_BEARER);
    }

    private String getJwtToken(final Auth auth) {
        String body = prepareBody(auth);
        HttpHeaders headers = getHeaders();
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        String response = doRequest(auth, request);
        if (StringUtils.isNotEmpty(response)) {
            DocumentContext context = JsonPath.parse(response);
            return context.read(AuthorizationConstant.CONTENT_KEY_TOKEN);
        }
        return DelimiterConstant.EMPTY;
    }

    private String prepareBody(final Auth auth) {
        return AuthUtil.getCredentialsFromFile(auth.getCredentials());
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.ALL));
        return headers;
    }

    private String doRequest(final Auth auth, final HttpEntity<String> request) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            return restTemplate.postForObject(getFullApiUrl(auth), request, String.class);
        } catch (HttpClientErrorException exception) {
            LogUtil.logResponseStatusError(exception);
        }
        return DelimiterConstant.EMPTY;
    }

    private String getFullApiUrl(final Auth auth) {
        List<Api> apiList = GlobalTestConfigurationProvider.getIntegrations().get(dependencies.getEnv())
                .getApis().getApi();
        Api apiIntegration = ConfigUtil.findApiForAlias(apiList, auth.getApiAlias());
        return apiIntegration.getUrl() + auth.getLoginEndpoint();
    }
}

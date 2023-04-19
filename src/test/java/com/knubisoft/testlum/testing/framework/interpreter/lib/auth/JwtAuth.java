package com.knubisoft.testlum.testing.framework.interpreter.lib.auth;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.constant.AuthorizationConstant;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.AuthUtil;
import com.knubisoft.testlum.testing.framework.util.IntegrationsUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.model.global_config.Api;
import com.knubisoft.testlum.testing.model.scenario.Auth;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

import static com.knubisoft.testlum.testing.framework.util.ResultUtil.AUTHENTICATION_TYPE;

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
        if (StringUtils.isNotBlank(response)) {
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
        List<Api> apiList = GlobalTestConfigurationProvider.getIntegrations().get(dependencies.getEnvironment())
                .getApis().getApi();
        Api apiIntegration = IntegrationsUtil.findApiForAlias(apiList, auth.getApiAlias());
        return apiIntegration.getUrl() + auth.getLoginEndpoint();
    }
}

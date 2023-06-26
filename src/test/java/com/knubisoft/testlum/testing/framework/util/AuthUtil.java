package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.model.global_config.Api;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

@UtilityClass
public class AuthUtil {
    @SneakyThrows
    public String getCredentialsFromFile(final String fileName) {
        return FileUtils.readFileToString(FileSearcher.searchFileFromDataFolder(fileName), StandardCharsets.UTF_8);
    }

    public Api getApiIntegration(final String alias, final String env) {
        List<Api> apiList = GlobalTestConfigurationProvider.getIntegrations().get(env).getApis().getApi();
        return IntegrationsUtil.findApiForAlias(apiList, alias);
    }
}

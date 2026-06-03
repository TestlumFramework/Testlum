package com.knubisoft.testlum.testing.framework.configuration.inbound;

import com.knubisoft.testlum.testing.framework.inbound.ngrok.NgrokConfigProvider;
import com.knubisoft.testlum.testing.framework.inbound.ngrok.NgrokTunnel;
import com.knubisoft.testlum.testing.framework.inbound.server.server.InboundServer;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.testlum.testing.model.global_config.HttpServer;
import com.knubisoft.testlum.testing.model.global_config.Ngrok;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NgrokConfiguration {

    @Bean
    public NgrokConfigProvider ngrokConfigProvider(GlobalTestConfiguration globalTestConfiguration) {
        HttpServer httpServer = globalTestConfiguration.getHttpServer();
        if (httpServer == null || httpServer.getNgrok() == null || !httpServer.isEnabled()) {
            return disabledNgrokConfigProvider();
        }
        Ngrok ngrokConfig = httpServer.getNgrok();
        return new NgrokConfigProvider() {
            @Override
            public boolean isEnabled() {
                return ngrokConfig.isEnabled();
            }

            @Override
            public String authToken() {
                return ngrokConfig.getAuthToken();
            }

            @Override
            public String reservedDomain() {
                return ngrokConfig.getReservedDomain();
            }
        };
    }

    @Bean(destroyMethod = "stop")
    public NgrokTunnel ngrokTunnel(NgrokConfigProvider config, InboundServer localServer) {
        if (!config.isEnabled()) {
            return null;
        }
        return new NgrokTunnel(config, localServer);
    }

    private NgrokConfigProvider disabledNgrokConfigProvider() {
        return new NgrokConfigProvider() {
            @Override
            public boolean isEnabled() {
                return false;
            }

            @Override
            public String authToken() {
                return "";
            }

            @Override
            public String reservedDomain() {
                return "";
            }
        };
    }

}

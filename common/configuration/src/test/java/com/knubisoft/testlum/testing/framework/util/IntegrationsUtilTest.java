package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.EnvToIntegrationMap;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.exception.IntegrationDisabledException;
import com.knubisoft.testlum.testing.model.global_config.Api;
import com.knubisoft.testlum.testing.model.global_config.Apis;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IntegrationsUtilTest {

    private IntegrationsUtil integrationsUtil;

    private Api createApi(final String alias, final boolean enabled) {
        final Api api = new Api();
        api.setAlias(alias);
        api.setEnabled(enabled);
        api.setUrl("http://localhost");
        return api;
    }

    @BeforeEach
    void setUp() {
        final Integrations integrations = new Integrations();
        final Apis apis = new Apis();
        apis.getApi().add(createApi("DEFAULT", true));
        apis.getApi().add(createApi("secondary", true));
        apis.getApi().add(createApi("disabled", false));
        integrations.setApis(apis);

        final EnvToIntegrationMap envMap = new EnvToIntegrationMap(Map.of("dev", integrations));
        integrationsUtil = new IntegrationsUtil(envMap);
    }

    @Nested
    class FindForAliasEnv {
        @Test
        void findsApiByAliasAndEnv() {
            final AliasEnv aliasEnv = new AliasEnv("DEFAULT", "dev");
            final Api result = integrationsUtil.findForAliasEnv(Api.class, aliasEnv);
            assertNotNull(result);
            assertEquals("DEFAULT", result.getAlias());
        }

        @Test
        void throwsForDisabledAlias() {
            final AliasEnv aliasEnv = new AliasEnv("disabled", "dev");
            assertThrows(IntegrationDisabledException.class,
                    () -> integrationsUtil.findForAliasEnv(Api.class, aliasEnv));
        }

        @Test
        void throwsForNonExistentAlias() {
            final AliasEnv aliasEnv = new AliasEnv("nonexistent", "dev");
            assertThrows(IntegrationDisabledException.class,
                    () -> integrationsUtil.findForAliasEnv(Api.class, aliasEnv));
        }
    }

    @Nested
    class FindListByEnv {
        @Test
        void returnsListForExistingEnv() {
            final List<Api> result = integrationsUtil.findListByEnv(Api.class, "dev");
            assertEquals(3, result.size());
        }

        @Test
        void returnsEmptyListForMissingEnv() {
            final List<Api> result = integrationsUtil.findListByEnv(Api.class, "nonexistent");
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class FindForAlias {
        @Test
        void findsEnabledIntegrationByAlias() {
            final List<Api> apis = integrationsUtil.findListByEnv(Api.class, "dev");
            final Api result = integrationsUtil.findForAlias(apis, "secondary");
            assertEquals("secondary", result.getAlias());
        }

        @Test
        void throwsForDisabledIntegration() {
            final List<Api> apis = integrationsUtil.findListByEnv(Api.class, "dev");
            assertThrows(IntegrationDisabledException.class,
                    () -> integrationsUtil.findForAlias(apis, "disabled"));
        }

        @Test
        void nullAliasDefaultsToDEFAULT() {
            final List<Api> apis = integrationsUtil.findListByEnv(Api.class, "dev");
            final Api result = integrationsUtil.findForAlias(apis, null);
            assertEquals("DEFAULT", result.getAlias());
        }
    }

    @Nested
    class FindApiForAlias {
        @Test
        void findsApiByAlias() {
            final List<Api> apis = integrationsUtil.findListByEnv(Api.class, "dev");
            final Api result = integrationsUtil.findApiForAlias(apis, "DEFAULT");
            assertNotNull(result);
        }

        @Test
        void throwsForMissingApi() {
            final List<Api> apis = integrationsUtil.findListByEnv(Api.class, "dev");
            assertThrows(IntegrationDisabledException.class,
                    () -> integrationsUtil.findApiForAlias(apis, "ghost"));
        }
    }

    @Nested
    class IsEnabled {
        @Test
        void returnsTrueWhenAnyEnabled() {
            final List<Api> apis = integrationsUtil.findListByEnv(Api.class, "dev");
            assertTrue(integrationsUtil.isEnabled(apis));
        }

        @Test
        void returnsFalseWhenNoneEnabled() {
            final Api disabledApi = createApi("only", false);
            assertFalse(integrationsUtil.isEnabled(List.of(disabledApi)));
        }

        @Test
        void returnsFalseForEmptyList() {
            assertFalse(integrationsUtil.isEnabled(Collections.emptyList()));
        }
    }
}

package com.testlum.testing.framework.interpreter.lib.cryptography;

import com.testlum.testing.framework.condition.AbstractCondition;
import com.testlum.testing.model.global_config.Cryptography;
import com.testlum.testing.model.global_config.CryptographyIntegrations;
import com.testlum.testing.model.global_config.Integration;
import com.testlum.testing.model.global_config.Integrations;

import java.util.List;
import java.util.Optional;

public class OnCryptographyEnabledCondition
        extends AbstractCondition<Cryptography> {
    @Override
    protected Optional<List<? extends Integration>> getIntegrations(
            final Optional<Integrations> integrations) {
        return integrations
                .map(Integrations::getCryptographyIntegrations)
                .map(CryptographyIntegrations::getCryptography);
    }
}

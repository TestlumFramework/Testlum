package com.testlum.testing.framework.condition;

import com.testlum.testing.model.global_config.Integration;
import com.testlum.testing.model.global_config.Integrations;
import com.testlum.testing.model.global_config.S3;
import com.testlum.testing.model.global_config.S3Integration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OnS3EnabledCondition extends AbstractCondition<S3> {

    @Override
    protected Optional<List<? extends Integration>> getIntegrations(final Optional<Integrations> integrations) {
        return integrations.map(Integrations::getS3Integration).map(S3Integration::getS3);
    }
}

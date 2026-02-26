package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.S3;
import com.knubisoft.testlum.testing.model.global_config.S3Integration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OnS3EnabledCondition extends AbstractCondition<S3> {

    @Override
    List<? extends Integration> getIntegrations(final Integrations integrations) {
        return Optional.ofNullable(integrations.getS3Integration())
                .map(S3Integration::getS3)
                .orElse(null);
    }
}

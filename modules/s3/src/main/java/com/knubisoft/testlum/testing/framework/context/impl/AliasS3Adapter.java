package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.condition.OnS3EnabledCondition;
import com.knubisoft.testlum.testing.framework.context.AbstractAliasAdapter;
import com.knubisoft.testlum.testing.framework.db.s3.S3Operation;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.List;

@Conditional({OnS3EnabledCondition.class})
@Component
public class AliasS3Adapter extends AbstractAliasAdapter {

    public AliasS3Adapter(final S3Operation s3Operation,
                          final Integrations integrations) {
        super(s3Operation, integrations);
    }

    @Override
    protected List<? extends Integration> getIntegrationList(final Integrations integrations) {
        return integrations.getS3Integration().getS3();
    }

    @Override
    protected String getStorageName() {
        return "S3";
    }
}

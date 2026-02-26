package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnS3EnabledCondition;
import com.knubisoft.testlum.testing.framework.constant.MigrationConstant;
import com.knubisoft.testlum.testing.framework.context.AliasAdapter;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.s3.S3Operation;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.S3;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.UNDERSCORE;

@Conditional({OnS3EnabledCondition.class})
@Component
@RequiredArgsConstructor
public class AliasS3Adapter implements AliasAdapter {

    private final S3Operation s3Operation;
    private final Integrations integrations;

    @Override
    public void apply(final Map<String, AbstractStorageOperation> aliasMap) {
        for (S3 s3 : integrations.getS3Integration().getS3()) {
            if (s3.isEnabled()) {
                aliasMap.put(MigrationConstant.S3 + UNDERSCORE + s3.getAlias(), s3Operation);
            }
        }
    }
}

package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnS3EnabledCondition;
import com.knubisoft.testlum.testing.framework.configuration.global.GlobalTestConfigurationProviderImpl.ConfigProvider;
import com.knubisoft.testlum.testing.framework.constant.MigrationConstant;
import com.knubisoft.testlum.testing.framework.context.AliasAdapter;
import com.knubisoft.testlum.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.testlum.testing.framework.context.NameToAdapterAliasImpl;
import com.knubisoft.testlum.testing.framework.db.s3.S3Operation;
import com.knubisoft.testlum.testing.model.global_config.S3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.UNDERSCORE;

@Conditional({OnS3EnabledCondition.class})
@Component
public class AliasS3Adapter implements AliasAdapter {

    @Autowired(required = false)
    private S3Operation s3Operation;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (S3 s3 : ConfigProvider.getDefaultIntegrations().getS3Integration().getS3()) {
            if (s3.isEnabled()) {
                aliasMap.put(MigrationConstant.S3 + UNDERSCORE + s3.getAlias(), getMetadataS3(s3));
            }
        }
    }

    private NameToAdapterAlias.Metadata getMetadataS3(final S3 s3) {
        return NameToAdapterAliasImpl.Metadata.builder()
                .configuration(s3)
                .storageOperation(s3Operation)
                .build();
    }
}

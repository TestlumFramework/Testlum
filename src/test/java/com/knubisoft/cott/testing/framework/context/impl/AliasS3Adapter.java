package com.knubisoft.cott.testing.framework.context.impl;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnS3EnabledCondition;
import com.knubisoft.cott.testing.framework.context.AliasAdapter;
import com.knubisoft.cott.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.cott.testing.framework.db.s3.S3Operation;
import com.knubisoft.cott.testing.model.global_config.S3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.UNDERSCORE;

@Conditional({OnS3EnabledCondition.class})
@Component
public class AliasS3Adapter implements AliasAdapter {

    @Autowired(required = false)
    private S3Operation s3Operation;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach(((s, integrations) -> addToAliasMap(s, integrations.getS3Integration().getS3(), aliasMap)));
    }

    private void addToAliasMap(final String envName,
                               final List<S3> s3List,
                               final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (S3 s3 : s3List) {
            if (s3.isEnabled()) {
                aliasMap.put(envName + UNDERSCORE + s3.getAlias(), getMetadataS3(s3));
            }
        }
    }

    private NameToAdapterAlias.Metadata getMetadataS3(final S3 s3) {
        return NameToAdapterAlias.Metadata.builder()
                .configuration(s3)
                .storageOperation(s3Operation)
                .build();
    }
}

package com.knubisoft.cott.testing.framework.db.mongodb;

import com.knubisoft.cott.testing.framework.env.EnvManager;
import com.knubisoft.cott.testing.framework.configuration.condition.OnMongoEnabledCondition;
import com.knubisoft.cott.testing.framework.db.StorageOperation;
import com.knubisoft.cott.testing.framework.db.source.Source;
import com.knubisoft.cott.testing.framework.util.JacksonMapperUtil;
import com.knubisoft.cott.testing.framework.env.AliasEnv;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Conditional({OnMongoEnabledCondition.class})
@Component
public class MongoOperation implements StorageOperation {

    private static final String DROP_DATABASE = "{dropDatabase: 1}";

    private final Map<AliasEnv, MongoDatabase> mongoDatabase;

    public MongoOperation(@Autowired(required = false) final Map<AliasEnv, MongoDatabase> mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
    }

    @Override
    public StorageOperationResult apply(final Source source, final String databaseAlias) {
        return new StorageOperationResult(applyQueries(source.getQueries(), databaseAlias));
    }

    @Override
    public void clearSystem() {
        mongoDatabase.forEach((aliasEnv, mongoDatabase) -> {
            if (Objects.equals(aliasEnv.getEnvironment(), EnvManager.currentEnv())) {
                mongoDatabase.runCommand(Document.parse(DROP_DATABASE));
            }
        });
    }

    private List<QueryResult<String>> applyQueries(final List<String> queries, final String databaseAlias) {
        return queries.stream()
                .map(query -> new QueryResult<>(query, executeQuery(query, databaseAlias)))
                .collect(Collectors.toList());
    }

    private String executeQuery(final String query, final String databaseAlias) {
        Document document = Document.parse(query);
        Document result = mongoDatabase.get(AliasEnv.build(databaseAlias)).runCommand(document);
        return JacksonMapperUtil.writeValueAsString(result);
    }
}

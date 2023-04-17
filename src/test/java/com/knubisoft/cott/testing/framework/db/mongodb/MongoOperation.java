package com.knubisoft.cott.testing.framework.db.mongodb;

import com.knubisoft.cott.testing.framework.configuration.condition.OnMongoEnabledCondition;
import com.knubisoft.cott.testing.framework.db.StorageOperation;
import com.knubisoft.cott.testing.framework.db.source.Source;
import com.knubisoft.cott.testing.framework.env.AliasEnv;
import com.knubisoft.cott.testing.framework.env.EnvManager;
import com.knubisoft.cott.testing.framework.util.JacksonMapperUtil;
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

    private final Map<AliasEnv, MongoDatabase> mongoDatabases;

    public MongoOperation(@Autowired(required = false) final Map<AliasEnv, MongoDatabase> mongoDatabases) {
        this.mongoDatabases = mongoDatabases;
    }

    @Override
    public StorageOperationResult apply(final Source source, final String databaseAlias) {
        return new StorageOperationResult(applyQueries(source.getQueries(), databaseAlias));
    }

    @Override
    public void clearSystem() {
        mongoDatabases.forEach((aliasEnv, database) -> {
            if (Objects.equals(aliasEnv.getEnvironment(), EnvManager.currentEnv())) {
                for (String collectionName : database.listCollectionNames()) {
                    database.getCollection(collectionName).drop();
                }
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
        Document result = mongoDatabases.get(AliasEnv.build(databaseAlias)).runCommand(document);
        return JacksonMapperUtil.writeValueAsString(result);
    }
}

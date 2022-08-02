package com.knubisoft.cott.testing.framework.db.mongodb;

import com.knubisoft.cott.testing.framework.db.StorageOperation;
import com.knubisoft.cott.testing.framework.util.JacksonMapperUtil;
import com.knubisoft.cott.testing.framework.db.source.ListSource;
import com.knubisoft.cott.testing.framework.db.source.Source;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MongoOperation implements StorageOperation {

    private static final String DROP_DATABASE = "{dropDatabase: 1}";

    private final Map<String, MongoDatabase> mongoDatabase;

    public MongoOperation(@Autowired(required = false) final Map<String, MongoDatabase> mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
    }

    @Override
    public StorageOperationResult apply(final Source source, final String databaseName) {
        return new StorageOperationResult(applyQueries(source.getQueries(), databaseName));
    }

    @Override
    public void clearSystem() {
        for (Map.Entry<String, MongoDatabase> entry : mongoDatabase.entrySet()) {
            apply(new ListSource(Collections.singletonList(DROP_DATABASE)), entry.getKey());
        }
    }

    private List<QueryResult<String>> applyQueries(final List<String> queries,
                                                   final String databaseName) {
        return queries.stream()
                .map(query -> new QueryResult<>(query, executeQuery(query, databaseName)))
                .collect(Collectors.toList());
    }

    private String executeQuery(final String query, final String databaseName) {
        Document document = Document.parse(query);
        Document result = mongoDatabase.get(databaseName).runCommand(document);
        return JacksonMapperUtil.writeValueAsString(result);
    }
}

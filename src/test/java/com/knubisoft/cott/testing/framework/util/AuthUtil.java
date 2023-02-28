package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.model.scenario.AbstractCommand;
import com.knubisoft.cott.testing.model.scenario.Clickhouse;
import com.knubisoft.cott.testing.model.scenario.Dynamo;
import com.knubisoft.cott.testing.model.scenario.Elasticsearch;
import com.knubisoft.cott.testing.model.scenario.Graphql;
import com.knubisoft.cott.testing.model.scenario.Http;
import com.knubisoft.cott.testing.model.scenario.Kafka;
import com.knubisoft.cott.testing.model.scenario.Migrate;
import com.knubisoft.cott.testing.model.scenario.Mongo;
import com.knubisoft.cott.testing.model.scenario.Mysql;
import com.knubisoft.cott.testing.model.scenario.Oracle;
import com.knubisoft.cott.testing.model.scenario.Postgres;
import com.knubisoft.cott.testing.model.scenario.Rabbit;
import com.knubisoft.cott.testing.model.scenario.Redis;
import com.knubisoft.cott.testing.model.scenario.S3;
import com.knubisoft.cott.testing.model.scenario.Sendgrid;
import com.knubisoft.cott.testing.model.scenario.Ses;
import com.knubisoft.cott.testing.model.scenario.Smtp;
import com.knubisoft.cott.testing.model.scenario.Sqs;
import com.knubisoft.cott.testing.model.scenario.Twilio;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@UtilityClass
public class AuthUtil {
    private static final Map<Class<? extends AbstractCommand>, Function<AbstractCommand, String>> COMMANDS_ALIASES_MAP;

    static {
        final Map<Class<? extends AbstractCommand>, Function<AbstractCommand, String>> map = new HashMap<>();
        map.put(Http.class, command -> ((Http) command).getAlias());
        map.put(Migrate.class, command -> ((Migrate) command).getAlias());
        map.put(Postgres.class, command -> ((Postgres) command).getAlias());
        map.put(Mysql.class, command -> ((Mysql) command).getAlias());
        map.put(Oracle.class, command -> ((Oracle) command).getAlias());
        map.put(Mongo.class, command -> ((Mongo) command).getAlias());
        map.put(Redis.class, command -> ((Redis) command).getAlias());
        map.put(Rabbit.class, command -> ((Rabbit) command).getAlias());
        map.put(Kafka.class, command -> ((Kafka) command).getAlias());
        map.put(S3.class, command -> ((S3) command).getAlias());
        map.put(Sqs.class, command -> ((Sqs) command).getAlias());
        map.put(Clickhouse.class, command -> ((Clickhouse) command).getAlias());
        map.put(Elasticsearch.class, command -> ((Elasticsearch) command).getAlias());
        map.put(Sendgrid.class, command -> ((Sendgrid) command).getAlias());
        map.put(Ses.class, command -> ((Ses) command).getAlias());
        map.put(Dynamo.class, command -> ((Dynamo) command).getAlias());
        map.put(Graphql.class, command -> ((Graphql) command).getAlias());
        map.put(Smtp.class, command -> ((Smtp) command).getAlias());
        map.put(Twilio.class, command -> ((Twilio) command).getAlias());
        COMMANDS_ALIASES_MAP = Collections.unmodifiableMap(map);
    }

    public static String getAliasFromAuthCommand(final AbstractCommand command,
                                                 final String authAlias) {
        String alias = COMMANDS_ALIASES_MAP.get(command.getClass()).apply(command);
        if (alias.equals(authAlias)) {
            return alias;
        } else {
            throw new DefaultFrameworkException("Alias from command doesn't match with alias from Auth");
        }
    }

    @SneakyThrows
    public String getCredentialsFromFile(final String fileName) {
        return FileUtils.readFileToString(FileSearcher.searchFileFromDataFolder(fileName), StandardCharsets.UTF_8);
    }
}

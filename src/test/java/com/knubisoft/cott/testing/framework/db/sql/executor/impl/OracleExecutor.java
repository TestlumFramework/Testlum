package com.knubisoft.cott.testing.framework.db.sql.executor.impl;

import com.knubisoft.cott.testing.framework.db.sql.executor.AbstractSqlExecutor;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class OracleExecutor extends AbstractSqlExecutor {

    private static final String SELECT_ORACLE_TABLE_NAMES = "SELECT table_name FROM all_tables WHERE owner = '%s'";
    private static final String DISABLE_ALL_TRIGGERS = "ALTER TABLE %s DISABLE ALL TRIGGERS";
    private static final String DISABLE_VALIDATE_PRIMARY_KEY = "ALTER TABLE %s DISABLE PRIMARY KEY";
    private static final String TRUNCATE_TABLES = "TRUNCATE TABLE %s";
    private static final String ENABLE_VALIDATE_PRIMARY_KEY = "ALTER TABLE %s ENABLE PRIMARY KEY";
    private static final String ENABLE_ALL_TRIGGERS = "ALTER TABLE %s ENABLE ALL TRIGGERS";

    //todo
    private static final List<String> TRUNCATE_QUERIES = Arrays.asList(
            DISABLE_ALL_TRIGGERS,
            DISABLE_VALIDATE_PRIMARY_KEY,
            TRUNCATE_TABLES,
            ENABLE_VALIDATE_PRIMARY_KEY,
            ENABLE_ALL_TRIGGERS);

    public OracleExecutor(final DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void truncate() {
        //todo
        requireNonNull(template).execute("declare\n" +
                "begin\n" +
                "    for c1 in (select y1.table_name, y1.constraint_name\n" +
                "               from user_constraints y1,\n" +
                "                    user_tables x1\n" +
                "               where x1.table_name = y1.table_name\n" +
                "               order by y1.r_constraint_name nulls last)\n" +
                "        loop\n" +
                "            begin\n" +
                "                dbms_output.put_line('alter table ' || c1.table_name || ' disable constraint ' || c1.constraint_name ||\n" +
                "                                     ';');\n" +
                "                execute immediate ('alter table ' || c1.table_name || ' disable constraint ' || c1.constraint_name);\n" +
                "            end;\n" +
                "        end loop;\n" +
                "\n" +
                "    for t1 in (select table_name from user_tables)\n" +
                "        loop\n" +
                "            begin\n" +
                "                dbms_output.put_line('truncate table ' || t1.table_name || ';');\n" +
                "                execute immediate ('truncate table ' || t1.table_name);\n" +
                "            end;\n" +
                "        end loop;\n" +
                "\n" +
                "    for c2 in (select y2.table_name, y2.constraint_name\n" +
                "               from user_constraints y2,\n" +
                "                    user_tables x2\n" +
                "               where x2.table_name = y2.table_name\n" +
                "               order by y2.r_constraint_name nulls first)\n" +
                "        loop\n" +
                "            begin\n" +
                "                dbms_output.put_line('alter table ' || c2.table_name || ' enable constraint ' || c2.constraint_name ||\n" +
                "                                     ';');\n" +
                "                execute immediate ('alter table ' || c2.table_name || ' enable constraint ' || c2.constraint_name);\n" +
                "            end;\n" +
                "        end loop;\n" +
                "\n" +
                "end;");
    }
}

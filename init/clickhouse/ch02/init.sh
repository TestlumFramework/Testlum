#!/usr/bin/env sh

CLICKHOUSE_DB="${CLICKHOUSE_DB:-database}";
CLICKHOUSE_USER="${CLICKHOUSE_USER:-user}";
CLICKHOUSE_PASSWORD="${CLICKHOUSE_PASSWORD:-password}";

cat <<EOT >> /etc/clickhouse-server/users.d/user.xml
<yandex>
  <!-- Docs: <https://clickhouse.tech/docs/en/operations/settings/settings_users/> -->
  <users>
    <${CLICKHOUSE_USER}>
      <profile>default</profile>
      <networks>
        <ip>::/0</ip>
      </networks>
      <password>${CLICKHOUSE_PASSWORD}</password>
      <quota>default</quota>
    </${CLICKHOUSE_USER}>
  </users>
</yandex>
EOT
cat /etc/clickhouse-server/users.d/user.xml;

clickhouse-client --query "CREATE DATABASE IF NOT EXISTS ${CLICKHOUSE_DB}"
clickhouse-client --database "${CLICKHOUSE_DB}" --user "${CLICKHOUSE_USER}" --password "${CLICKHOUSE_PASSWORD}" --query "CREATE TABLE IF NOT EXISTS t_user
                           (   id             Int256,
                               username       String,
                               email          String,
                               email_verified UInt8      DEFAULT FALSE,
                               first_name     String,
                               last_name      String,
                               password       String,
                               is_active      UInt8      DEFAULT FALSE,
                               last_login     DateTime64,
                               locked         UInt8      DEFAULT FALSE,
                               locked_cause   String,
                               created_at     DateTime64 DEFAULT now(),
                               created_by     Int256,
                               deleted_at     DateTime64,
                               deleted_by     Int256) ENGINE = Memory"
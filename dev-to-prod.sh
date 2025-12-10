#!/usr/bin/env bash
set -euo pipefail

export PATH="/opt/homebrew/bin:$PATH"

### DEV database config
DEV_HOST="162.55.97.228"
DEV_PORT="25433"
DEV_DB="testlum_dev_db"
DEV_USER="${PSQL_USER_DEV}"
DEV_PASSWORD="${PSQL_PASS_DEV}"

### PROD database config
PROD_HOST="162.55.97.228"
PROD_PORT="25432"
PROD_DB="testlum_prod_db"
PROD_USER="${PSQL_USER_PROD}"
PROD_PASSWORD="${PSQL_PASS_PROD}"

### Schema name
SCHEMA="testlum_web_api"

### Tables to sync
TABLES=(
"t_post"
"t_policy"
"t_expert"
"t_customer_feedback"
"t_component"
"t_card"
"t_button"
"t_faq_topic"
"t_faq"
"t_technology"
"t_team_size"
"t_case_study"
"t_case_study_technology"
"t_case_study_industry"
"t_tag"
"t_post_tag"
"t_faq_tags"
"t_pricing_features_card"
"t_pricing_feature"
"t_author"
"t_framework"
"t_industry"
"t_guide"
"t_routing_path"
)

### S3 CONFIG
export AWS_ACCESS_KEY_ID={$AWS_ACCESS_KEY_ID}"
export AWS_SECRET_ACCESS_KEY={$AWS_SECRET_ACCESS_KEY}"
export AWS_DEFAULT_REGION="eu-north-1"

DEV_BUCKET="testlum-build"
PROD_BUCKET="testlum-build-prod"

# URL patterns as stored in DB
DEV_VIRTUAL_PREFIX="https://testlum-web-dev.s3.eu-north-1.amazonaws.com"
PROD_VIRTUAL_PREFIX="https://testlum-web-prod.s3.eu-north-1.amazonaws.com"

DEV_PATH_PREFIX="https://s3.eu-north-1.amazonaws.com/testlum-web-dev"
PROD_PATH_PREFIX="https://s3.eu-north-1.amazonaws.com/testlum-web-prod"

echo "=== STARTING DEV → PROD SYNC ==="

echo "[CHECK] Connecting to DEV DB..."
PGPASSWORD="$DEV_PASSWORD" \
psql -h "$DEV_HOST" -p "$DEV_PORT" -U "$DEV_USER" -d "$DEV_DB" -c "SELECT 1;" >/dev/null

echo "[CHECK] Connecting to PROD DB..."
PGPASSWORD="$PROD_PASSWORD" \
psql -h "$PROD_HOST" -p "$PROD_PORT" -U "$PROD_USER" -d "$PROD_DB" -c "SELECT 1;" >/dev/null

echo "[CHECK] Checking access to PROD S3 bucket s3://$PROD_BUCKET ..."
aws s3 ls "s3://$PROD_BUCKET" >/dev/null 2>&1 || {
echo "[ERROR] Cannot access PROD bucket: s3://$PROD_BUCKET"
exit 1
}
echo "[OK] PROD bucket is accessible."

echo "[STEP] Creating data dump from DEV..."

DUMP_FILE="$(mktemp "/tmp/dev_to_prod_XXXXXX.sql")"
echo "[INFO] Dump file: $DUMP_FILE"

PGPASSWORD="$DEV_PASSWORD" \
pg_dump \
-h "$DEV_HOST" -p "$DEV_PORT" -U "$DEV_USER" \
--data-only \
--inserts \
$(for T in "${TABLES[@]}"; do printf -- "--table=%s.%s " "$SCHEMA" "$T"; done) \
"$DEV_DB" \
> "$DUMP_FILE"

echo "[OK] Dump from DEV created."

echo "[STEP] Collecting DEV S3 URLs from DEV DB..."

URLS=$(
PGPASSWORD="$DEV_PASSWORD" \
psql -h "$DEV_HOST" -p "$DEV_PORT" -U "$DEV_USER" -d "$DEV_DB" -At <<SQL
SELECT DISTINCT image_url
FROM "$SCHEMA"."t_post"
WHERE image_url LIKE '${DEV_VIRTUAL_PREFIX}%'
OR image_url LIKE '${DEV_PATH_PREFIX}%'

UNION
SELECT DISTINCT image_url
FROM "$SCHEMA"."t_expert"
WHERE image_url LIKE '${DEV_VIRTUAL_PREFIX}%'
OR image_url LIKE '${DEV_PATH_PREFIX}%'

UNION
SELECT DISTINCT image_url
FROM "$SCHEMA"."t_customer_feedback"
WHERE image_url LIKE '${DEV_VIRTUAL_PREFIX}%'
OR image_url LIKE '${DEV_PATH_PREFIX}%'

UNION
SELECT DISTINCT img_url
FROM "$SCHEMA"."t_component"
WHERE img_url LIKE '${DEV_VIRTUAL_PREFIX}%'
OR img_url LIKE '${DEV_PATH_PREFIX}%'

UNION
SELECT DISTINCT img_url
FROM "$SCHEMA"."t_card"
WHERE img_url LIKE '${DEV_VIRTUAL_PREFIX}%'
OR img_url LIKE '${DEV_PATH_PREFIX}%'

UNION
SELECT DISTINCT logo
FROM "$SCHEMA"."t_case_study"
WHERE logo LIKE '${DEV_VIRTUAL_PREFIX}%'
OR logo LIKE '${DEV_PATH_PREFIX}%'

UNION
SELECT DISTINCT main_image
FROM "$SCHEMA"."t_case_study"
WHERE main_image LIKE '${DEV_VIRTUAL_PREFIX}%'
OR main_image LIKE '${DEV_PATH_PREFIX}%'

UNION
SELECT DISTINCT photo_url
FROM "$SCHEMA"."t_author"
WHERE photo_url LIKE '${DEV_VIRTUAL_PREFIX}%'
OR photo_url LIKE '${DEV_PATH_PREFIX}%';
SQL
)

if [ -z "$URLS" ]; then
echo "[INFO] No DEV S3 URLs found in DEV DB (image/img_url/photo_url fields)."
else
echo "[INFO] Found DEV URLs to process:"
echo "$URLS"
fi

echo "[STEP] Copying S3 objects DEV → PROD ..."

while IFS= read -r url; do
[ -z "$url" ] && continue

key=""
if [[ "$url" == ${DEV_VIRTUAL_PREFIX}/* ]]; then
    key="${url#${DEV_VIRTUAL_PREFIX}/}"
elif [[ "$url" == ${DEV_PATH_PREFIX}/* ]]; then
    key="${url#${DEV_PATH_PREFIX}/}"
else
    echo "[WARN] Unknown URL format, skipping: $url"
    continue
fi

echo "[S3 COPY] s3://${DEV_BUCKET}/${key}  →  s3://${PROD_BUCKET}/${key}"

aws s3 cp \
    "s3://${DEV_BUCKET}/${key}" \
    "s3://${PROD_BUCKET}/${key}" \
    --region "$AWS_DEFAULT_REGION"

done <<< "$URLS"

echo "[OK] All S3 copies finished."

echo "[STEP] Applying dump and URL replacements on PROD in a single transaction..."

PGPASSWORD="$PROD_PASSWORD" \
psql -h "$PROD_HOST" -p "$PROD_PORT" -U "$PROD_USER" -d "$PROD_DB" <<SQL
BEGIN;

ALTER TABLE "$SCHEMA"."t_post_tag" DISABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_faq_tags" DISABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_case_study_technology" DISABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_case_study_industry" DISABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_pricing_features_card" DISABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_case_study" DISABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_tag" DISABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_faq" DISABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_faq_topic" DISABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_button" DISABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_card" DISABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_component" DISABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_customer_feedback" DISABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_expert" DISABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_post" DISABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_policy" DISABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_technology" DISABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_team_size" DISABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_pricing_feature" DISABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_author" DISABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_framework" DISABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_industry" DISABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_guide" DISABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_routing_path" DISABLE TRIGGER ALL;

TRUNCATE TABLE
"$SCHEMA"."t_post_tag",
"$SCHEMA"."t_faq_tags",
"$SCHEMA"."t_case_study_technology",
"$SCHEMA"."t_case_study_industry",
"$SCHEMA"."t_pricing_features_card",
"$SCHEMA"."t_case_study",
"$SCHEMA"."t_tag",
"$SCHEMA"."t_faq",
"$SCHEMA"."t_faq_topic",
"$SCHEMA"."t_button",
"$SCHEMA"."t_card",
"$SCHEMA"."t_component",
"$SCHEMA"."t_customer_feedback",
"$SCHEMA"."t_expert",
"$SCHEMA"."t_post",
"$SCHEMA"."t_policy",
"$SCHEMA"."t_technology",
"$SCHEMA"."t_team_size",
"$SCHEMA"."t_pricing_feature",
"$SCHEMA"."t_author",
"$SCHEMA"."t_framework",
"$SCHEMA"."t_industry",
"$SCHEMA"."t_guide",
"$SCHEMA"."t_routing_path"
RESTART IDENTITY CASCADE;

\\i $DUMP_FILE

UPDATE "$SCHEMA"."t_post"
SET image_url = REPLACE(image_url, '${DEV_VIRTUAL_PREFIX}', '${PROD_VIRTUAL_PREFIX}')
WHERE image_url LIKE '${DEV_VIRTUAL_PREFIX}%';

UPDATE "$SCHEMA"."t_post"
SET image_url = REPLACE(image_url, '${DEV_PATH_PREFIX}', '${PROD_PATH_PREFIX}')
WHERE image_url LIKE '${DEV_PATH_PREFIX}%';

UPDATE "$SCHEMA"."t_expert"
SET image_url = REPLACE(image_url, '${DEV_VIRTUAL_PREFIX}', '${PROD_VIRTUAL_PREFIX}')
WHERE image_url LIKE '${DEV_VIRTUAL_PREFIX}%';

UPDATE "$SCHEMA"."t_expert"
SET image_url = REPLACE(image_url, '${DEV_PATH_PREFIX}', '${PROD_PATH_PREFIX}')
WHERE image_url LIKE '${DEV_PATH_PREFIX}%';

UPDATE "$SCHEMA"."t_customer_feedback"
SET image_url = REPLACE(image_url, '${DEV_VIRTUAL_PREFIX}', '${PROD_VIRTUAL_PREFIX}')
WHERE image_url LIKE '${DEV_VIRTUAL_PREFIX}%';

UPDATE "$SCHEMA"."t_customer_feedback"
SET image_url = REPLACE(image_url, '${DEV_PATH_PREFIX}', '${PROD_PATH_PREFIX}')
WHERE image_url LIKE '${DEV_PATH_PREFIX}%';

UPDATE "$SCHEMA"."t_component"
SET img_url = REPLACE(img_url, '${DEV_VIRTUAL_PREFIX}', '${PROD_VIRTUAL_PREFIX}')
WHERE img_url LIKE '${DEV_VIRTUAL_PREFIX}%';

UPDATE "$SCHEMA"."t_component"
SET img_url = REPLACE(img_url, '${DEV_PATH_PREFIX}', '${PROD_PATH_PREFIX}')
WHERE img_url LIKE '${DEV_PATH_PREFIX}%';

UPDATE "$SCHEMA"."t_card"
SET img_url = REPLACE(img_url, '${DEV_VIRTUAL_PREFIX}', '${PROD_VIRTUAL_PREFIX}')
WHERE img_url LIKE '${DEV_VIRTUAL_PREFIX}%';

UPDATE "$SCHEMA"."t_card"
SET img_url = REPLACE(img_url, '${DEV_PATH_PREFIX}', '${PROD_PATH_PREFIX}')
WHERE img_url LIKE '${DEV_PATH_PREFIX}%';

UPDATE "$SCHEMA"."t_case_study"
SET logo = REPLACE(logo, '${DEV_VIRTUAL_PREFIX}', '${PROD_VIRTUAL_PREFIX}')
WHERE logo LIKE '${DEV_VIRTUAL_PREFIX}%';

UPDATE "$SCHEMA"."t_case_study"
SET logo = REPLACE(logo, '${DEV_PATH_PREFIX}', '${PROD_PATH_PREFIX}')
WHERE logo LIKE '${DEV_PATH_PREFIX}%';

UPDATE "$SCHEMA"."t_case_study"
SET main_image = REPLACE(main_image, '${DEV_VIRTUAL_PREFIX}', '${PROD_VIRTUAL_PREFIX}')
WHERE main_image LIKE '${DEV_VIRTUAL_PREFIX}%';

UPDATE "$SCHEMA"."t_case_study"
SET main_image = REPLACE(main_image, '${DEV_PATH_PREFIX}', '${PROD_PATH_PREFIX}')
WHERE main_image LIKE '${DEV_PATH_PREFIX}%';

UPDATE "$SCHEMA"."t_author"
SET photo_url = REPLACE(photo_url, '${DEV_VIRTUAL_PREFIX}', '${PROD_VIRTUAL_PREFIX}')
WHERE photo_url LIKE '${DEV_VIRTUAL_PREFIX}%';

UPDATE "$SCHEMA"."t_author"
SET photo_url = REPLACE(photo_url, '${DEV_PATH_PREFIX}', '${PROD_PATH_PREFIX}')
WHERE photo_url LIKE '${DEV_PATH_PREFIX}%';

ALTER TABLE "$SCHEMA"."t_post_tag" ENABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_faq_tags" ENABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_case_study_technology" ENABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_case_study_industry" ENABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_pricing_features_card" ENABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_case_study" ENABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_tag" ENABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_faq" ENABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_faq_topic" ENABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_button" ENABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_card" ENABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_component" ENABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_customer_feedback" ENABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_expert" ENABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_post" ENABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_policy" ENABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_technology" ENABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_team_size" ENABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_pricing_feature" ENABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_author" ENABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_framework" ENABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_industry" ENABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_guide" ENABLE TRIGGER ALL;
ALTER TABLE "$SCHEMA"."t_routing_path" ENABLE TRIGGER ALL;

COMMIT;
SQL

echo "[CLEANUP] Removing dump file..."
rm -f "$DUMP_FILE"

echo "=== SYNC COMPLETED SUCCESSFULLY ==="
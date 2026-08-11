#!/usr/bin/env bash
# Runs once on first init of the postgres container (mounted into
# /docker-entrypoint-initdb.d/). Creates one database per microservice,
# owned by $POSTGRES_USER, skipping any that already exist.
#
# To onboard a new service's database, just append its name to DATABASES.
set -euo pipefail

DATABASES=(
  "health_base_user"
  "health_base_clinic"
)

for db in "${DATABASES[@]}"; do
  exists=$(psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" --tuples-only --no-align \
    -c "SELECT 1 FROM pg_database WHERE datname = '${db}'")

  if [ "$exists" = "1" ]; then
    echo "Database '${db}' already exists, skipping."
  else
    echo "Creating database '${db}'..."
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" \
      -c "CREATE DATABASE \"${db}\" OWNER \"$POSTGRES_USER\";"
  fi
done

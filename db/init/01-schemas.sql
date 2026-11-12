-- DBA step: the database and one schema per module exist before the app starts.
-- Liquibase only creates tables; Flowable creates its own tables in `workflow`.
-- Used by compose (docker-entrypoint-initdb.d), Testcontainers, and the DBA in prod.
-- Nothing is ever created in `public`.
CREATE SCHEMA IF NOT EXISTS request;
CREATE SCHEMA IF NOT EXISTS review;
CREATE SCHEMA IF NOT EXISTS tracking;
CREATE SCHEMA IF NOT EXISTS workflow;
CREATE SCHEMA IF NOT EXISTS common;

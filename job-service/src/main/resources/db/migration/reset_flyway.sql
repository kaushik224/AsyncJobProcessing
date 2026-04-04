-- Run this manually in your database to reset Flyway for development
-- This will drop the schema history table and allow migrations to re-run

DROP TABLE IF EXISTS flyway_schema_history;
DROP TABLE IF EXISTS jobs;

-- Drop all objects in the public schema
DROP SCHEMA public CASCADE;

-- Recreate the public schema
CREATE SCHEMA public;

-- Grant privileges
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO public;

-- Drop Flyway schema history table if it exists
DROP TABLE IF EXISTS flyway_schema_history; 
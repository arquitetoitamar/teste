DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'parking') THEN
        CREATE DATABASE parking;
    END IF;
END
$$; 
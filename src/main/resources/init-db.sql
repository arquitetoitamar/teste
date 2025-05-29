-- Create database if not exists
CREATE DATABASE parking_management;

-- Connect to the database
\c parking_management;

-- Create extension for UUID if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp"; 
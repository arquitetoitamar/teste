-- Create tables
CREATE TABLE IF NOT EXISTS garage_sector (
    id BIGSERIAL PRIMARY KEY,
    sector VARCHAR(50) NOT NULL UNIQUE,
    base_price DECIMAL(10,2) NOT NULL,
    max_capacity INTEGER NOT NULL,
    open_hour TIME NOT NULL,
    close_hour TIME NOT NULL,
    duration_limit_minutes INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS spot (
    id VARCHAR(50) PRIMARY KEY,
    sector_id BIGINT NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    FOREIGN KEY (sector_id) REFERENCES garage_sector(id)
);

CREATE TABLE IF NOT EXISTS parking_event (
    id BIGSERIAL PRIMARY KEY,
    spot_id VARCHAR(50) NOT NULL,
    event_type VARCHAR(20) NOT NULL,
    event_time TIMESTAMP NOT NULL,
    FOREIGN KEY (spot_id) REFERENCES spot(id)
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_spot_sector ON spot(sector_id);
CREATE INDEX IF NOT EXISTS idx_parking_event_spot ON parking_event(spot_id);
CREATE INDEX IF NOT EXISTS idx_parking_event_time ON parking_event(event_time); 
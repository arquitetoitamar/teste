-- Create garage_sectors table
CREATE TABLE IF NOT EXISTS garage_sectors (
    id VARCHAR(10) PRIMARY KEY,
    base_price DECIMAL(10,2) NOT NULL,
    max_capacity INTEGER NOT NULL,
    open_hour TIME NOT NULL,
    close_hour TIME NOT NULL,
    duration_limit_minutes INTEGER NOT NULL,
    current_occupancy INTEGER DEFAULT 0
);

-- Create parking_spots table
CREATE TABLE IF NOT EXISTS parking_spots (
    id SERIAL PRIMARY KEY,
    sector_id VARCHAR(10) NOT NULL,
    spot_number VARCHAR(10) NOT NULL,
    latitude DECIMAL(10,8) NOT NULL,
    longitude DECIMAL(11,8) NOT NULL,
    occupied BOOLEAN DEFAULT FALSE,
    license_plate VARCHAR(10),
    FOREIGN KEY (sector_id) REFERENCES garage_sectors(id)
);

-- Create parking_events table
CREATE TABLE IF NOT EXISTS parking_events (
    id SERIAL PRIMARY KEY,
    license_plate VARCHAR(10) NOT NULL,
    type VARCHAR(10) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    sector_id VARCHAR(10),
    FOREIGN KEY (sector_id) REFERENCES garage_sectors(id)
); 
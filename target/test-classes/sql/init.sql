-- Clean up existing data
DELETE FROM parking_events;
DELETE FROM parking_spots;
DELETE FROM garage_sectors;

-- Insert test sectors
INSERT INTO garage_sectors (id, base_price, max_capacity, open_hour, close_hour, duration_limit_minutes, current_occupancy) VALUES
('A1', 10.00, 50, '06:00', '22:00', 120, 0),
('A2', 12.00, 30, '06:00', '22:00', 180, 0),
('B1', 15.00, 20, '06:00', '22:00', 240, 0); 
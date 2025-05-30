-- Insert garage sectors
INSERT INTO garage_sectors (id, base_price, max_capacity, open_hour, close_hour, duration_limit_minutes, current_occupancy) VALUES
('A', 10.00, 50, '06:00', '22:00', 120, 0),
('A2', 12.00, 30, '06:00', '22:00', 180, 0),
('B1', 15.00, 20, '06:00', '22:00', 240, 0);

-- Insert parking spots
INSERT INTO parking_spots (sector_id, latitude, longitude, occupied) VALUES
('A', -23.550520, -46.633308, false),
('A', -23.550521, -46.633309, false),
('A2', -23.550522, -46.633310, false),
('A2', -23.550523, -46.633311, false),
('B1', -23.550524, -46.633312, false),
('B1', -23.550525, -46.633313, false); 
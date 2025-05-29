-- Insert garage sectors
INSERT INTO garage_sectors (id, base_price, max_capacity, open_hour, close_hour, duration_limit_minutes) VALUES
('A', 10.00, 50, '06:00', '22:00', 120),
('A2', 12.00, 30, '06:00', '22:00', 180),
('B1', 15.00, 20, '06:00', '22:00', 240);

-- Insert parking spots
INSERT INTO parking_spots (sector_id, spot_number, latitude, longitude) VALUES
('A', 'A-001', -23.550520, -46.633308),
('A', 'A1-002', -23.550521, -46.633309),
('A2', 'A2-001', -23.550522, -46.633310),
('A2', 'A2-002', -23.550523, -46.633311),
('B1', 'B1-001', -23.550524, -46.633312),
('B1', 'B1-002', -23.550525, -46.633313); 
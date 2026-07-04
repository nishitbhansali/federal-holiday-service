-- Sample Federal Holidays for USA
INSERT INTO federal_holiday (holiday_name, holiday_date, country, is_recurring, description, created_at, updated_at) VALUES
('New Year''s Day', '2026-01-01', 'USA', true, 'First day of the year', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Martin Luther King Jr. Day', '2026-01-19', 'USA', true, 'Birthday of Martin Luther King Jr.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Presidents'' Day', '2026-02-16', 'USA', true, 'Washington''s Birthday', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Memorial Day', '2026-05-25', 'USA', true, 'Remembrance of fallen soldiers', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Independence Day', '2026-07-04', 'USA', true, 'Independence of the United States', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Labor Day', '2026-09-07', 'USA', true, 'Celebration of workers', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Columbus Day', '2026-10-12', 'USA', true, 'Arrival of Columbus to Americas', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Veterans Day', '2026-11-11', 'USA', true, 'Honoring military veterans', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Thanksgiving Day', '2026-11-26', 'USA', true, 'Thanksgiving celebration', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Christmas Day', '2026-12-25', 'USA', true, 'Christmas celebration', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Sample Federal Holidays for Canada
INSERT INTO federal_holiday (holiday_name, holiday_date, country, is_recurring, description, created_at, updated_at) VALUES
('New Year''s Day', '2026-01-01', 'CANADA', true, 'First day of the year', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Good Friday', '2026-04-03', 'CANADA', true, 'Friday before Easter Sunday', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Easter Monday', '2026-04-06', 'CANADA', true, 'Monday after Easter Sunday', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Victoria Day', '2026-05-18', 'CANADA', true, 'Queen Victoria''s birthday celebration', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Canada Day', '2026-07-01', 'CANADA', true, 'National Day of Canada', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Labour Day', '2026-09-07', 'CANADA', true, 'Celebration of workers', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Thanksgiving', '2026-10-12', 'CANADA', true, 'Canadian Thanksgiving', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Remembrance Day', '2026-11-11', 'CANADA', true, 'Remembrance of war veterans', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Christmas Day', '2026-12-25', 'CANADA', true, 'Christmas celebration', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Boxing Day', '2026-12-26', 'CANADA', true, 'Day after Christmas', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

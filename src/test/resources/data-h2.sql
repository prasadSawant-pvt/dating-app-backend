-- Insert test user
INSERT INTO app_user (id, email, first_name, last_name, password, active, created_at) 
VALUES ('test-user-1', 'alice@example.com', 'Alice', 'Smith', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', true, CURRENT_TIMESTAMP);

-- Insert test user profile
INSERT INTO profile (id, user_id, display_name, bio, date_of_birth, gender, latitude, longitude, experience_yrs)
VALUES ('test-user-1', 'test-user-1', 'Alice Smith', 'Software developer who loves hiking and photography', '1990-05-15', 'FEMALE', 37.7749, -122.4194, 5);

-- Insert interests
INSERT INTO profile_interests (profile_id, interest) VALUES 
('test-user-1', 'Hiking'),
('test-user-1', 'Photography'),
('test-user-1', 'Coding');

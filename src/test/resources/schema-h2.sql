-- Create app_user table
CREATE TABLE IF NOT EXISTS app_user (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- Create profile table
CREATE TABLE IF NOT EXISTS profile (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    bio TEXT,
    date_of_birth DATE,
    gender VARCHAR(20),
    latitude DOUBLE,
    longitude DOUBLE,
    experience_yrs INTEGER,
    FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
);

-- Create profile_interests table (for interests collection)
CREATE TABLE IF NOT EXISTS profile_interests (
    profile_id VARCHAR(36) NOT NULL,
    interest VARCHAR(100) NOT NULL,
    PRIMARY KEY (profile_id, interest),
    FOREIGN KEY (profile_id) REFERENCES profile(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_profile_user_id ON profile(user_id);
CREATE INDEX IF NOT EXISTS idx_profile_interests_profile_id ON profile_interests(profile_id);

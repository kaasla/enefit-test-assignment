-- Squashed baseline creating final schema + seed data

-- Tables
CREATE TABLE resources (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL CHECK (type IN ('METERING_POINT', 'CONNECTION_POINT')),
    country_code VARCHAR(2) NOT NULL CHECK (country_code ~ '^[A-Z]{2}$'),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_resource_country_code ON resources(country_code);
CREATE INDEX idx_resource_type ON resources(type);

CREATE TABLE locations (
    id BIGSERIAL PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    street_address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    postal_code VARCHAR(5) NOT NULL,
    country_code VARCHAR(2) NOT NULL CHECK (country_code ~ '^[A-Z]{2}$'),
    
    CONSTRAINT fk_location_resource 
        FOREIGN KEY (resource_id) 
        REFERENCES resources(id) 
        ON DELETE CASCADE,
        
    CONSTRAINT uq_location_resource 
        UNIQUE (resource_id),
    
    CONSTRAINT chk_postal_code_format CHECK (postal_code ~ '^[0-9]{5}$')
    
    -- Note: Country code matching constraint is enforced at application level
);

CREATE TABLE characteristics (
    id BIGSERIAL PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    code VARCHAR(5) NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('CONSUMPTION_TYPE', 'CHARGING_POINT', 'CONNECTION_POINT_STATUS')),
    char_value VARCHAR(255) NOT NULL,
    
    CONSTRAINT fk_characteristic_resource 
        FOREIGN KEY (resource_id) 
        REFERENCES resources(id) 
        ON DELETE CASCADE
);

CREATE INDEX idx_characteristic_code ON characteristics(code);
CREATE INDEX idx_characteristic_type ON characteristics(type);
CREATE INDEX idx_characteristic_resource_id ON characteristics(resource_id);

-- Seed data (from previous V2)
INSERT INTO resources (type, country_code, version, created_at, updated_at) VALUES
('METERING_POINT', 'US', 0, NOW(), NOW()),
('CONNECTION_POINT', 'DE', 0, NOW(), NOW()),
('METERING_POINT', 'FR', 0, NOW(), NOW());

INSERT INTO locations (resource_id, street_address, city, postal_code, country_code) VALUES
(1, '123 Main Street', 'New York', '10001', 'US'),
(2, 'Unter den Linden 1', 'Berlin', '10117', 'DE'),
(3, '25 Rue de la Paix', 'Paris', '75002', 'FR');

INSERT INTO characteristics (resource_id, code, type, char_value) VALUES
-- US Resource characteristics
(1, 'CT01', 'CONSUMPTION_TYPE', 'RESIDENTIAL'),
(1, 'CP01', 'CHARGING_POINT', 'TYPE2'),

-- DE Resource characteristics  
(2, 'CPS01', 'CONNECTION_POINT_STATUS', 'ACTIVE'),
(2, 'CT02', 'CONSUMPTION_TYPE', 'COMMERCIAL'),

-- FR Resource characteristics
(3, 'CT03', 'CONSUMPTION_TYPE', 'INDUSTRIAL'),
(3, 'CP02', 'CHARGING_POINT', 'CCS');

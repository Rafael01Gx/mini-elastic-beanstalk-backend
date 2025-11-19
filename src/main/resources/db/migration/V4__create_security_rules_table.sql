CREATE TABLE security_rules (
    id BIGSERIAL PRIMARY KEY,
    server_id VARCHAR(36) NOT NULL REFERENCES servers(id) ON DELETE CASCADE,
    direction VARCHAR(20) NOT NULL,
    protocol VARCHAR(10) NOT NULL,
    port_from INTEGER,
    port_to INTEGER,
    allowed_ips TEXT[],
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_security_rules_server_id ON security_rules(server_id);
CREATE TABLE deploys (
    id BIGSERIAL PRIMARY KEY,
    server_id VARCHAR(36) NOT NULL REFERENCES servers(id) ON DELETE CASCADE,
    workspace VARCHAR(255) NOT NULL,
    compose_path TEXT,
    env_path TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_deploys_server_id ON deploys(server_id);
CREATE INDEX idx_deploys_status ON deploys(status);
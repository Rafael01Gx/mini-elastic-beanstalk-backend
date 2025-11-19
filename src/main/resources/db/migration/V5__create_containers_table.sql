CREATE TABLE containers (
    id VARCHAR(64) PRIMARY KEY,
    server_id VARCHAR(36) NOT NULL REFERENCES servers(id) ON DELETE CASCADE,
    deploy_id BIGINT REFERENCES deploys(id) ON DELETE SET NULL,
    name VARCHAR(255) NOT NULL,
    image VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    ports JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_containers_server_id ON containers(server_id);
CREATE INDEX idx_containers_status ON containers(status);
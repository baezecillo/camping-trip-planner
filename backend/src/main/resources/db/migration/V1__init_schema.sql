CREATE TABLE users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE trips (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    origin      VARCHAR(255) NOT NULL,
    destination VARCHAR(255) NOT NULL,
    start_date  DATE NOT NULL,
    end_date    DATE NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trips_user FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT uq_trips_one_active_per_user UNIQUE (user_id)
);

CREATE TABLE checklist_items (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    trip_id   BIGINT NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    category  VARCHAR(50)  NOT NULL,
    is_packed BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_checklist_trip FOREIGN KEY (trip_id) REFERENCES trips(id)
        ON DELETE CASCADE
);

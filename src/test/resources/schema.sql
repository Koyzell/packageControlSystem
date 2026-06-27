CREATE TABLE IF NOT EXISTS packages (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    tracking_number VARCHAR(64)  NOT NULL,
    recipient_phone VARCHAR(20)  NOT NULL,
    courier_company VARCHAR(32)  NOT NULL,
    shelf_location  VARCHAR(32)  NOT NULL,
    pickup_code     VARCHAR(20)  DEFAULT NULL,
    status          VARCHAR(16)  NOT NULL DEFAULT 'AWAITING_PICKUP',
    check_in_time   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    check_out_time  TIMESTAMP    DEFAULT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tracking_number (tracking_number),
    UNIQUE KEY uk_pickup_code (pickup_code),
    INDEX idx_recipient_phone (recipient_phone),
    INDEX idx_status (status),
    INDEX idx_check_in_time (check_in_time)
);

CREATE TABLE IF NOT EXISTS users (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    username    VARCHAR(32)  NOT NULL,
    password    VARCHAR(128) NOT NULL,
    role        VARCHAR(16)  NOT NULL DEFAULT 'USER',
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
);

CREATE TABLE IF NOT EXISTS operation_logs (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    package_id      BIGINT       DEFAULT NULL,
    tracking_number VARCHAR(64)  DEFAULT NULL,
    operation_type  VARCHAR(16)  NOT NULL,
    operator        VARCHAR(32)  DEFAULT NULL,
    details         VARCHAR(256) DEFAULT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_created_at (created_at)
);

CREATE TABLE IF NOT EXISTS shelves (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    name        VARCHAR(16) NOT NULL,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_shelf_name (name)
);

CREATE TABLE IF NOT EXISTS couriers (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    name        VARCHAR(32) NOT NULL,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_courier_name (name)
);

INSERT INTO shelves (name) SELECT 'A-01' WHERE NOT EXISTS (SELECT 1 FROM shelves WHERE name = 'A-01');
INSERT INTO shelves (name) SELECT 'A-02' WHERE NOT EXISTS (SELECT 1 FROM shelves WHERE name = 'A-02');
INSERT INTO shelves (name) SELECT 'B-01' WHERE NOT EXISTS (SELECT 1 FROM shelves WHERE name = 'B-01');
INSERT INTO shelves (name) SELECT 'B-02' WHERE NOT EXISTS (SELECT 1 FROM shelves WHERE name = 'B-02');
INSERT INTO shelves (name) SELECT 'C-01' WHERE NOT EXISTS (SELECT 1 FROM shelves WHERE name = 'C-01');

INSERT INTO couriers (name) SELECT '顺丰速运' WHERE NOT EXISTS (SELECT 1 FROM couriers WHERE name = '顺丰速运');
INSERT INTO couriers (name) SELECT '圆通速递' WHERE NOT EXISTS (SELECT 1 FROM couriers WHERE name = '圆通速递');
INSERT INTO couriers (name) SELECT '中通速递' WHERE NOT EXISTS (SELECT 1 FROM couriers WHERE name = '中通速递');
INSERT INTO couriers (name) SELECT '韵达快递' WHERE NOT EXISTS (SELECT 1 FROM couriers WHERE name = '韵达快递');
INSERT INTO couriers (name) SELECT '申通快递' WHERE NOT EXISTS (SELECT 1 FROM couriers WHERE name = '申通快递');
INSERT INTO couriers (name) SELECT '京东物流' WHERE NOT EXISTS (SELECT 1 FROM couriers WHERE name = '京东物流');
INSERT INTO couriers (name) SELECT '极兔速递' WHERE NOT EXISTS (SELECT 1 FROM couriers WHERE name = '极兔速递');
INSERT INTO couriers (name) SELECT '邮政快递' WHERE NOT EXISTS (SELECT 1 FROM couriers WHERE name = '邮政快递');
INSERT INTO couriers (name) SELECT '其他' WHERE NOT EXISTS (SELECT 1 FROM couriers WHERE name = '其他');

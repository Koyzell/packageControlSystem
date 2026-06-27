CREATE DATABASE IF NOT EXISTS parcel_station
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE parcel_station;

CREATE TABLE IF NOT EXISTS packages (
    id              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    tracking_number VARCHAR(64)  NOT NULL                 COMMENT '运单号',
    recipient_phone VARCHAR(20)  NOT NULL                 COMMENT '收件人手机号',
    courier_company VARCHAR(32)  NOT NULL                 COMMENT '快递公司',
    shelf_location  VARCHAR(32)  NOT NULL                 COMMENT '货架位置',
    pickup_code     VARCHAR(20)  DEFAULT NULL             COMMENT '取件码(如260626-A-001)',
    status          VARCHAR(16)  NOT NULL DEFAULT 'AWAITING_PICKUP'
                                                          COMMENT '状态: AWAITING_PICKUP(待取件) / PICKED_UP(已取件)',
    check_in_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                                          COMMENT '入库时间',
    check_out_time  DATETIME     DEFAULT NULL             COMMENT '出库时间（取件后记录）',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tracking_number (tracking_number),
    UNIQUE KEY uk_pickup_code (pickup_code),
    INDEX idx_recipient_phone (recipient_phone),
    INDEX idx_status (status),
    INDEX idx_check_in_time (check_in_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='驿站包裹记录表';

CREATE TABLE IF NOT EXISTS users (
    id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    username    VARCHAR(32)  NOT NULL                 COMMENT '用户名',
    password    VARCHAR(128) NOT NULL                 COMMENT 'BCrypt加密密码',
    role        VARCHAR(16)  NOT NULL DEFAULT 'USER'  COMMENT '角色: ADMIN/USER',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='用户表';

CREATE TABLE IF NOT EXISTS operation_logs (
    id              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    package_id      BIGINT       DEFAULT NULL             COMMENT '关联包裹ID',
    tracking_number VARCHAR(64)  DEFAULT NULL             COMMENT '运单号(冗余)',
    operation_type  VARCHAR(16)  NOT NULL                 COMMENT '操作类型: CHECK_IN/PICK_UP',
    operator        VARCHAR(32)  DEFAULT NULL             COMMENT '操作人用户名',
    details         VARCHAR(256) DEFAULT NULL             COMMENT '操作详情',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='操作日志表';

CREATE TABLE IF NOT EXISTS shelves (
    id          BIGINT      NOT NULL AUTO_INCREMENT  COMMENT '主键',
    name        VARCHAR(16) NOT NULL                 COMMENT '货架名称',
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_shelf_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='货架表';

CREATE TABLE IF NOT EXISTS couriers (
    id          BIGINT      NOT NULL AUTO_INCREMENT  COMMENT '主键',
    name        VARCHAR(32) NOT NULL                 COMMENT '快递公司名称',
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_courier_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='快递公司表';

-- Seed shelves
INSERT IGNORE INTO shelves (name) VALUES ('A-01'), ('A-02'), ('B-01'), ('B-02'), ('C-01');

-- Seed couriers
INSERT IGNORE INTO couriers (name) VALUES
('顺丰速运'), ('圆通速递'), ('中通速递'), ('韵达快递'), ('申通快递'),
('京东物流'), ('极兔速递'), ('邮政快递'), ('其他');

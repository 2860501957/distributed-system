-- 主库配置
CREATE DATABASE IF NOT EXISTS seckill_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建主从复制账号
CREATE USER 'repl'@'%' IDENTIFIED BY 'repl123456';
GRANT REPLICATION SLAVE ON *.* TO 'repl'@'%';
FLUSH PRIVILEGES;

-- 初始化商品数据（用于测试）
USE seckill_db;
INSERT INTO product (name, description, price, create_time, update_time)
VALUES 
('秒杀商品1', '高并发测试商品1', 99.9, NOW(), NOW()),
('秒杀商品2', '高并发测试商品2', 199.9, NOW(), NOW());
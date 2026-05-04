package com.wuhan.seckill.common.util;

public class SnowflakeIdGenerator {
    // 起始时间戳（2020-01-01）
    private static final long START_TIMESTAMP = 1577836800000L;
    // 机器ID和数据中心ID的位数
    private static final long WORKER_ID_BITS = 5L;
    private static final long DATA_CENTER_ID_BITS = 5L;
    // 序列号位数
    private static final long SEQUENCE_BITS = 12L;

    // 最大值
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    // 偏移量
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;

    private final long workerId;
    private final long dataCenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator(long workerId, long dataCenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException("workerId 超出范围");
        }
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
            throw new IllegalArgumentException("dataCenterId 超出范围");
        }
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }

    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨，拒绝生成ID");
        }
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT) |
                (dataCenterId << DATA_CENTER_ID_SHIFT) |
                (workerId << WORKER_ID_SHIFT) |
                sequence;
    }

    private long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
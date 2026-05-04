package com.wuhan.seckill.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.wuhan.seckill.common.kafka.producer.SeckillOrderProducer;
import com.wuhan.seckill.common.kafka.transaction.TransactionMessage;
import com.wuhan.seckill.order.entity.Order;
import com.wuhan.seckill.order.mapper.OrderMapper;
import com.wuhan.seckill.order.service.OrderService;
import com.wuhan.seckill.transaction.mapper.TransactionLogMapper;
import com.wuhan.seckill.transaction.entity.TransactionLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private TransactionLogMapper transactionLogMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private static final String TRANSACTION_TOPIC = "seckill_transaction_topic";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(Order order) {
        // 1. 生成事务ID
        String transactionId = UUID.randomUUID().toString().replace("-", "");

        // 2. 保存订单
        orderMapper.insert(order);

        // 3. 写入事务日志（本地事务）
        TransactionLog log = new TransactionLog();
        log.setTransactionId(transactionId);
        log.setOrderId(order.getId());
        log.setProductId(order.getProductId());
        log.setQuantity(order.getQuantity());
        log.setStatus(0); // 待处理
        transactionLogMapper.insert(log);

        // 4. 发送事务消息到Kafka，通知库存服务扣减库存
        TransactionMessage message = new TransactionMessage();
        message.setTransactionId(transactionId);
        message.setOrderId(order.getId());
        message.setProductId(order.getProductId());
        message.setQuantity(order.getQuantity());
        kafkaTemplate.send(TRANSACTION_TOPIC, JSON.toJSONString(message));

        return order;
    }
}
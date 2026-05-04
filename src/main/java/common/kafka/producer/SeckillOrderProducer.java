package com.wuhan.seckill.common.kafka.producer;

import com.alibaba.fastjson.JSON;
import com.wuhan.seckill.seckill.entity.SeckillOrderMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class SeckillOrderProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private static final String SECKILL_ORDER_TOPIC = "seckill_order_topic";

    public void sendOrderMessage(SeckillOrderMessage message) {
        String jsonMessage = JSON.toJSONString(message);
        kafkaTemplate.send(SECKILL_ORDER_TOPIC, jsonMessage);
    }
}
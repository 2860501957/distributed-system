package com.wuhan.seckill.common.kafka.consumer;

import com.alibaba.fastjson.JSON;
import com.wuhan.seckill.order.entity.Order;
import com.wuhan.seckill.order.service.OrderService;
import com.wuhan.seck.product.entity.Product;
import com.wuhan.seckill.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SeckillOrderConsumer {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    private static final String SECKILL_ORDER_TOPIC = "seckill_order_topic";

    @KafkaListener(topics = SECKILL_ORDER_TOPIC, groupId = "seckill-order-group")
    @Transactional(rollbackFor = Exception.class)
    public void consumeOrderMessage(String message) {
        SeckillOrderMessage orderMessage = JSON.parseObject(message, SeckillOrderMessage.class);
        Long userId = orderMessage.getUserId();
        Long productId = orderMessage.getProductId();
        Long orderId = orderMessage.getOrderId();
        Integer quantity = orderMessage.getQuantity();

        // 1. 查询商品信息
        Product product = productService.getProductById(productId);
        if (product == null) {
            return;
        }

        // 2. 创建订单
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setTotalPrice(product.getPrice().multiply(new java.math.BigDecimal(quantity)));
        order.setStatus(1); // 已支付状态
        orderService.createOrder(order);

        // 3. 扣减数据库库存（和Redis库存保持最终一致）
        // 此处省略库存扣减逻辑，可调用库存服务的扣减方法
    }
}
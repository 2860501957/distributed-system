package com.wuhan.seckill.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.wuhan.seckill.common.result.Result;
import com.wuhan.seckill.common.util.RedisUtils;
import com.wuhan.seckill.common.util.SnowflakeIdGenerator;
import com.wuhan.seckill.common.kafka.producer.SeckillOrderProducer;
import com.wuhan.seckill.product.entity.Product;
import com.wuhan.seckill.product.service.ProductService;
import com.wuhan.seckill.seckill.entity.SeckillOrderMessage;
import com.wuhan.seckill.seckill.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SeckillOrderProducer orderProducer;

    @Autowired
    private ProductService productService;

    // 雪花算法ID生成器（workerId和dataCenterId可根据部署节点配置）
    private SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(1, 1);

    // 库存缓存key前缀
    private static final String STOCK_KEY_PREFIX = "seckill:stock:";
    // 防重key前缀（用户+商品）
    private static final String REPEAT_CHECK_KEY = "seckill:repeat:";

   @Override
public Result<String> seckill(Long userId, Long productId, Integer quantity) {
    // 1. 基础参数校验
    if (userId == null || productId == null || quantity <= 0) {
        return Result.error("参数错误");
    }

    // 2. 防重校验
    String repeatKey = REPEAT_CHECK_KEY + userId + ":" + productId;
    if (stringRedisTemplate.hasKey(repeatKey)) {
        return Result.error("请勿重复下单");
    }

    // 3. Redis预扣减库存（原子操作，防超卖）
    String stockKey = STOCK_KEY_PREFIX + productId;
    Long remainStock = stringRedisTemplate.opsForValue().decrement(stockKey, quantity);
    if (remainStock < 0) {
        stringRedisTemplate.opsForValue().increment(stockKey, quantity);
        return Result.error("商品已售罄");
    }

    // 4. 生成订单ID
    Long orderId = idGenerator.nextId();

    // 5. 构建订单对象
    Order order = new Order();
    order.setId(orderId);
    order.setUserId(userId);
    order.setProductId(productId);
    order.setQuantity(quantity);
    order.setTotalPrice(productService.getProductById(productId).getPrice().multiply(new BigDecimal(quantity)));
    order.setStatus(0); // 待支付

    // 6. 调用订单服务创建订单（基于消息的分布式事务）
    orderService.createOrder(order);

    // 7. 设置防重标记
    stringRedisTemplate.opsForValue().set(repeatKey, "1", 5, TimeUnit.MINUTES);

    return Result.success("秒杀请求已提交，订单号：" + orderId);
}
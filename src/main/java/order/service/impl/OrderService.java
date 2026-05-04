package com.wuhan.seckill.order.service;

import com.wuhan.seckill.common.transaction.context.TccContext;
import com.wuhan.seckill.order.entity.Order;

public interface OrderService {
    Order createOrder(Order order);

    // TCC Try：冻结订单状态，标记为待支付
    boolean tryPayOrder(TccContext context);

    // TCC Confirm：更新订单状态为已支付
    boolean confirmPayOrder(TccContext context);

    // TCC Cancel：取消支付，订单状态回滚
    boolean cancelPayOrder(TccContext context);
}
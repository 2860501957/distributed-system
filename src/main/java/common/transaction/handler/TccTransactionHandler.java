package com.wuhan.seckill.common.transaction.handler;

import com.wuhan.seckill.common.transaction.annotation.TccTransaction;
import com.wuhan.seckill.common.transaction.context.TccContext;
import com.wuhan.seckill.order.service.OrderService;
import com.wuhan.seckill.stock.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TccTransactionHandler {

    @Autowired
    private OrderService orderService;

    @Autowired
    private StockService stockService;

    @TccTransaction(
            tryMethod = "tryPayAndStock",
            confirmMethod = "confirmPayAndStock",
            cancelMethod = "cancelPayAndStock"
    )
    public boolean processTccTransaction(TccContext context) {
        // 1. Try阶段：订单+库存同时冻结
        boolean orderTry = orderService.tryPayOrder(context);
        boolean stockTry = stockService.tryDecreaseStock(context);
        if (!orderTry || !stockTry) {
            // 任一失败，执行Cancel回滚
            cancelPayAndStock(context);
            return false;
        }
        context.setStatus(1);
        return true;
    }

    public boolean confirmPayAndStock(TccContext context) {
        // 2. Confirm阶段：订单+库存同时确认
        boolean orderConfirm = orderService.confirmPayOrder(context);
        boolean stockConfirm = stockService.confirmDecreaseStock(context);
        if (orderConfirm && stockConfirm) {
            context.setStatus(2);
            return true;
        }
        // 任一失败，执行Cancel回滚
        cancelPayAndStock(context);
        return false;
    }

    public boolean cancelPayAndStock(TccContext context) {
        // 3. Cancel阶段：订单+库存同时回滚
        orderService.cancelPayOrder(context);
        stockService.cancelDecreaseStock(context);
        context.setStatus(3);
        return true;
    }
}
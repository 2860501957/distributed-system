package com.wuhan.seckill.stock.service;

import com.wuhan.seckill.common.transaction.context.TccContext;

public interface StockService {
    boolean decreaseStock(Long productId, Integer quantity);

    // TCC Try：预扣减库存（冻结）
    boolean tryDecreaseStock(TccContext context);

    // TCC Confirm：确认扣减库存
    boolean confirmDecreaseStock(TccContext context);

    // TCC Cancel：恢复冻结库存
    boolean cancelDecreaseStock(TccContext context);
}
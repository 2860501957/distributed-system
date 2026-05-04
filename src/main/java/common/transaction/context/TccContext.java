package com.wuhan.seckill.common.transaction.context;

import lombok.Data;

@Data
public class TccContext {
    private String transactionId;
    private Long orderId;
    private Long userId;
    private Long productId;
    private Integer status; // 0:TRY 1:CONFIRM 2:CANCEL
}
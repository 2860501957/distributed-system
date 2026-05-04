package com.wuhan.seckill.common.kafka.transaction;

import lombok.Data;
import java.io.Serializable;

@Data
public class TransactionMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private String transactionId;
    private Long orderId;
    private Long productId;
    private Integer quantity;
}
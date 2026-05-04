@Override
@Transactional(rollbackFor = Exception.class)
public boolean tryDecreaseStock(TccContext context) {
    Long productId = context.getProductId();
    Integer quantity = context.getQuantity();
    Stock stock = stockMapper.selectByProductId(productId);
    if (stock == null || stock.getQuantity() < quantity) {
        return false;
    }
    // Try：冻结库存，减少可用库存，增加冻结库存
    stock.setAvailableQuantity(stock.getAvailableQuantity() - quantity);
    stock.setFrozenQuantity(stock.getFrozenQuantity() + quantity);
    stockMapper.updateById(stock);
    return true;
}

@Override
@Transactional(rollbackFor = Exception.class)
public boolean confirmDecreaseStock(TccContext context) {
    Long productId = context.getProductId();
    Integer quantity = context.getQuantity();
    Stock stock = stockMapper.selectByProductId(productId);
    if (stock == null || stock.getFrozenQuantity() < quantity) {
        return false;
    }
    // Confirm：确认扣减，减少冻结库存
    stock.setFrozenQuantity(stock.getFrozenQuantity() - quantity);
    stockMapper.updateById(stock);
    return true;
}

@Override
@Transactional(rollbackFor = Exception.class)
public boolean cancelDecreaseStock(TccContext context) {
    Long productId = context.getProductId();
    Integer quantity = context.getQuantity();
    Stock stock = stockMapper.selectByProductId(productId);
    if (stock == null || stock.getFrozenQuantity() < quantity) {
        return false;
    }
    // Cancel：恢复库存，增加可用库存，减少冻结库存
    stock.setAvailableQuantity(stock.getAvailableQuantity() + quantity);
    stock.setFrozenQuantity(stock.getFrozenQuantity() - quantity);
    stockMapper.updateById(stock);
    return true;
}
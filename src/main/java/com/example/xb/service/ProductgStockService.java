package com.example.xb.service;

import com.example.xb.domain.product.ProductgStock;

import java.math.BigDecimal;
import java.util.List;

public interface ProductgStockService {
    /**
     * 指定型号产品的库存
     * @param productgId
     * @return
     */
    List<ProductgStock> productgStockList(String productgId);

    /**
     * 创建指定型号产品的库存
     * @param productgStock
     * @return
     */
    int saveProductgStock(ProductgStock productgStock);


    /**
     * 更新指定型号产品库存
     * @param productgStock
     * @return
     */
    int updateStock(ProductgStock productgStock);

    /**
     * 删除指定型号产品的库存记录
     * @param productgId
     * @return
     */
    int deleteStock(String productgId);
}

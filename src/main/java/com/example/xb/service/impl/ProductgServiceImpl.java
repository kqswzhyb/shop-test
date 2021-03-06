package com.example.xb.service.impl;

import com.example.xb.domain.product.AttrDetail;
import com.example.xb.domain.product.ProductAttr;
import com.example.xb.domain.product.Productg;
import com.example.xb.domain.product.ProductgStock;
import com.example.xb.domain.vo.ProductgVo;
import com.example.xb.mapper.ProductgMapper;
import com.example.xb.service.ProductAttrService;
import com.example.xb.service.ProductgService;
import com.example.xb.service.ProductgStockService;
import com.example.xb.utils.UUIDUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductgServiceImpl implements ProductgService {
    @Autowired
    private ProductgMapper productgMapper;
    @Autowired
    private ProductAttrService productAttrService;
    @Autowired
    private ProductgStockService productgStockService;

    @Override
    public List<Productg> productgList(String productId) {
        return productgMapper.productgList(productId);
    }

    @Override
    public List<ProductgVo> productgListAll(String productId) {
        return productgMapper.productgListAll(productId);
    }

    @Override
    public int batchSaveProductg(List<Productg> list) {
        for(Productg child:list) {
          List<AttrDetail> attrList = child.getAttrList();
          productAttrService.deleteProductAttrById(child.getProductgId());
          productgStockService.deleteStock(child.getProductgId());
          ProductgStock productgStock = new ProductgStock();
          productgStock.setLockStock(new BigDecimal("0"));
          productgStock.setTotalStock(new BigDecimal("0"));
          productgStock.setProductgId(child.getProductgId());
          productgStock.setStatus("0");
          productgStock.setStockId(UUIDUtil.NewUUID());
          productgStockService.saveProductgStock(productgStock);
          if(attrList.size()!=0) {
              List<ProductAttr> newList = new ArrayList<>();
              for(AttrDetail attr:attrList) {
                  ProductAttr productAttr = new ProductAttr();
                  productAttr.setAttrSonId(attr.getAttrSonValue());
                  productAttr.setProductgId(child.getProductgId());
                  newList.add(productAttr);
              }
              productAttrService.batchSaveProductAttr(newList);
          }
        }
        return productgMapper.batchSaveProductg(list);
    }

    @Override
    public int deleteProductgById(String productId) {
        List<Productg> list = productgMapper.productgList(productId);
        productAttrService.deleteProductAttrById(list.get(0).getProductgId());
        productgStockService.deleteStock(list.get(0).getProductgId());
        return productgMapper.deleteProductgById(productId);
    }
}

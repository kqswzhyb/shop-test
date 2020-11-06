package com.example.xb.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Product extends Base{
    private String productId;

    private String productCode;

    private String name;

    private String productUnit;

    private String brandId;

    private String status;
}
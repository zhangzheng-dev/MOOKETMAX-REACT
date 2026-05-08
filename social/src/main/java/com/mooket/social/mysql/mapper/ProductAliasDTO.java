package com.mooket.social.mysql.mapper;

/**
 * 产品别名DTO（用于JOIN查询结果）
 */
public class ProductAliasDTO {
    private String productName;
    private String aliasName;

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getAliasName() { return aliasName; }
    public void setAliasName(String aliasName) { this.aliasName = aliasName; }
}

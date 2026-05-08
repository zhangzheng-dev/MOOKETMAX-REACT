package com.mooket.social.service;

/**
 * 品牌 Service
 */
public interface BrandService {

    /**
     * 获取品牌详情
     *
     * @param brandName 品牌名称
     * @param category 品类（牛/猪）
     * @param type 报盘类型：offer(报盘) 或 inquiry(求购)
     * @param sortBy 排序方式：comprehensive(综合) 或 price(价格)
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     */
    com.mooket.social.dto.BrandDetailDTO getBrandDetail(String brandName, String category, String type, String sortBy, int page, int pageSize);

    /**
     * 获取品牌+产品详情（品牌+产品搜索结果页）
     *
     * @param brandName 品牌名称
     * @param productName 产品名称
     * @param category 品类
     * @param type 报盘类型：offer 或 inquiry
     * @param sortBy 排序方式
     * @param page 页码
     * @param pageSize 每页大小
     */
    com.mooket.social.dto.BrandDetailDTO getBrandProductDetail(String brandName, String productName, String category, String type, String sortBy, int page, int pageSize);
}
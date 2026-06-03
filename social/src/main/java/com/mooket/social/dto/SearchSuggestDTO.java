package com.mooket.social.dto;

import lombok.Data;

/**
 * 搜索联想词 DTO
 */
@Data
public class SearchSuggestDTO {

    private String text;          // 联想显示文本（用于展示）
    private String keyword;       // 原始输入关键词
    private String type;          // 类型
    private Integer priority;     // 优先级（1-7，数字越小优先级越高）
private Long targetId;     // 目标ID
    private String matchType;     // 匹配类型：product/factory/brand/merchant/combined
    private String inputKeyword;  // 用户输入的原始关键词（用于别名显示判断）
    private String standardName;  // 标准名称（如果输入匹配的是别名）
    private String aliasName;     // 别名（如果输入匹配的是别名）
    private String country;       // 标准国家名（用于前端跳转和搜索历史）
    private String factoryNo;     // 标准厂号
    private String productName;   // 标准产品名
    private String brandName;     // 标准品牌名
    private String merchantName;  // 标准商家名
}

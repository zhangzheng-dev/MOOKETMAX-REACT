package com.mooket.social.entity.mysql;

import lombok.Data;

/**
 * MySQL 源数据表 social_standard_goods_name 对应实体
 */
@Data
public class SocialStandardGoodsName {
    private Long id;
    private String standardGoodsName;
    private Integer goodsCategory;  // 产品分类（关联sys_dict）
    private String createdTime;
    private String updateTime;
}

package com.mooket.social.entity.mysql;

import lombok.Data;

/**
 * MySQL 源数据表 social_standard_goods_name_detail 对应实体
 */
@Data
public class SocialStandardGoodsNameDetail {
    private Long id;
    private Long standardGoodsNameId;    // 关联 social_standard_goods_name.id
    private String associatedGoodsName; // 别名
}

package com.mooket.social.entity.mysql;

import lombok.Data;

/**
 * MySQL 源数据表 social_group_factory_no 对应实体
 * 品牌/集团与厂号的关联表
 */
@Data
public class SocialGroupFactoryNo {

    private Long id;

    private String groupName;      // 品牌/集团名称（映射到 dict_brand.brand_name）

    private String groupAlias;    // 品牌别名（映射到 dict_brand.alias_list）

    private String factoryNo;      // 厂号（映射到 dict_brand.factory_no）

    private Integer goodsCategory; // 产品大类（映射到 dict_brand.category: 1=牛, 2=羊, 3=猪, 4=禽, 5=水产）
}

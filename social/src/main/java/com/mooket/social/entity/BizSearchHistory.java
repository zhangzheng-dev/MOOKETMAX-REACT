package com.mooket.social.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 搜索历史实体
 */
@Data
@TableName("biz_search_history")
public class BizSearchHistory {

    @TableId(type = IdType.AUTO)
    private Long historyId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 搜索词
     */
    private String searchWord;

    /**
     * 搜索类型（产品/国家/品牌/商家/国家厂号/国家产品/品牌产品/国家厂号产品）
     */
    private String searchType;

    /**
     * 是否自选（0-否，1-是）
     */
    private Integer isSelfSelect;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 国家
     */
    private String country;

    /**
     * 厂号
     */
    private String factoryNo;

    /**
     * 品牌ID
     */
    private Long brandId;

    /**
     * 商家ID
     */
    private Long merchantId;
}

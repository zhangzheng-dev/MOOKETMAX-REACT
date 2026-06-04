package com.mooket.social.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 报盘/求购实体
 */
@Data
@TableName("biz_offer")
public class BizOffer {

    @TableId(type = IdType.AUTO)
    private Long offerId;

    private Long sourceBusinessId;      // 源表 social_online_business.id

    private String offerOriginalText;   // 报盘原文

    private String category;            // 大类（牛/猪）

    private Integer productId;          // 产品ID

    private String productName;         // 产品标准名称

    private String country;             // 国家

    private String factoryNo;           // 厂号

    private Long factoryId;          // 关联厂号ID

    private Integer brandId;           // 关联品牌ID

    private Long merchantId;         // 关联商家ID

    private String contactPhone;        // 联系电话

    private Long userId;                // 发布人用户ID

    private String userNickname;        // 发布人昵称

    private BigDecimal price;           // 单价（元/千克）

    private BigDecimal priceMax;        // 最高单价

    private String weight;              // 重量

    private String offerType;           // 数据类型（报盘/求购）

    private String goodsType;           // 货物类型（现货/期货/半期货）

    private String goodsLocation;       // 货物所在地

    private String tags;                // 标签列表

    private String fatRatio;            // 瘦肉率

    private String feedingType;         // 饲养方式（草饲/谷饲）

    private String cattleBreed;         // 牛种

    private String remark;              // 备注

    private String packageId;            // 打包售卖ID（为空则表示不打包售卖）

    private BigDecimal packagePrice;     // 打包价（元）

    private String packageTotalQuantity; // 打包总数量（如"2柜、27吨、1500件"）

    private String packageSellType;      // 打包售卖方式（打包售卖/可单出）

    private LocalDateTime publishTime;  // 发布时间

    private LocalDate dataDate;         // 数据日期

    private String status;              // 状态（ACTIVE/INACTIVE）

    private LocalDateTime createTime;
}

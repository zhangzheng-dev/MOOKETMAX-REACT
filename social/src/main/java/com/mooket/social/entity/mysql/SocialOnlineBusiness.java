package com.mooket.social.entity.mysql;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MySQL 源数据表 social_online_business 对应实体
 */
@Data
public class SocialOnlineBusiness {

    private Long id;                  // 主键ID
    private Long userId;              // 用户ID
    private Integer isOffer;          // 是否报盘(0.求购 1.报盘)
    private Integer goodsCategory;    // 产品分类
    private String goodsName;         // 产品名称
    private Long standardGoodsNameId; // 对应的标准品名id
    private Integer country;          // 原产国
    private String plantNo;           // 厂号
    private Integer businessCategory; // 报盘类型/货物类型
    private String addressProvince;   // 货物地-省份
    private Long addressProvinceId;   // 货物地-省份id
    private String addressCity;       // 货物地-城市
    private Long addressCityId;       // 货物地-城市id
    private String standard;          // 规格
    private String standardTwo;       // 牛肉规格
    private Integer leanRatio;       // 瘦肉率
    private BigDecimal amount;       // 报价
    private Integer amountUnit;       // 报价单位
    private BigDecimal weight;        // 重量
    private Integer weightUnit;       // 重量单位
    private LocalDateTime offerDate;  // 报盘时间
    private String userName;          // 用户名称
    private String phoneNo;           // 电话号码
    private Integer status;           // 状态(1.待上架 2.未上架 3.已上架)
    private String memo;              // 备注
    private LocalDateTime onlineTime; // 上架时间
    private Integer displayFlag;      // 按需快搜是否展示
    private String hashCodeA;         // 大类+产品+国家厂号+饲养方式+商家hash code
    private Long onlineBusinessContentId; // 全网询报盘文本表id
    private Integer isDeleted;        // 是否删除(0.否 1.是)
    private LocalDateTime createdTime; // 创建时间
    private LocalDateTime updateTime; // 修改时间
}

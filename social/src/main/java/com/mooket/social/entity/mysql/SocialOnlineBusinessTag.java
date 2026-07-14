package com.mooket.social.entity.mysql;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * MySQL 源数据表 social_online_business_tag 对应实体
 * 标签表：一行一个标签，通过 online_business_id 关联 social_online_business.id
 */
@Data
public class SocialOnlineBusinessTag {

    private Long id;                 // 主键ID
    private Long onlineBusinessId;   // 全网报盘id → social_online_business.id
    private String businessTag;      // 标签值
    private LocalDateTime updateTime;// 修改时间
}

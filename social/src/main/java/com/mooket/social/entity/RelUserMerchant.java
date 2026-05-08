package com.mooket.social.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户商家关联实体
 */
@Data
@TableName("rel_user_merchant")
public class RelUserMerchant {

    @TableId(type = IdType.AUTO)
    private Long userId;

    private String mobile;

    private String nickname;

    private String identity;

    private Integer merchantId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

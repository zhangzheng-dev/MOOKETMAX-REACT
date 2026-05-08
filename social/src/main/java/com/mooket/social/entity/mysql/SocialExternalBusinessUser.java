package com.mooket.social.entity.mysql;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * MySQL social_external_business_user 表实体 (mallee_muji_social)
 */
@Data
public class SocialExternalBusinessUser {

    private Long id;

    private String mobileNo;

    private String nickName;

    private Long industryGroupId;

    private LocalDateTime createdTime;

    private LocalDateTime updateTime;
}

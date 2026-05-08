package com.mooket.social.entity.uac;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * MySQL uac_industry_group 表实体 (mallee_muji_uac)
 */
@Data
public class UacIndustryGroup {

    private Long id;

    private String industryGroupName;

    private String industryGroupNameAbbreviation;

    private LocalDateTime createdTime;

    private LocalDateTime updateTime;
}

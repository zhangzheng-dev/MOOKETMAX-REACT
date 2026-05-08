package com.mooket.social.entity.erp;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * MySQL erp_base_approval 表实体 (mallee_muji_erp)
 */
@Data
public class ErpBaseApproval {

    private Long id;

    private Integer category;

    private String country;

    private String plantNo;

    private String plantName;

    private Integer plantStatus;

    private LocalDateTime createdTime;

    private LocalDateTime updateTime;
}

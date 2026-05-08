package com.mooket.social.entity.mysql;

import lombok.Data;

/**
 * MySQL 源数据表 sys_dict 对应实体
 */
@Data
public class SysDict {
    private String dictKey;
    private String dictValue;
    private String dictNameEn;
}

package com.mooket.social.erp.mapper;

import com.mooket.social.entity.erp.ErpBaseApproval;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MySQL erp_base_approval 表 Mapper (mallee_muji_erp)
 */
@Mapper
public interface ErpBaseApprovalMapper {

    /**
     * 查询所有有效数据（全量同步用）
     */
    @Select("SELECT id, category, country, plant_no, plant_name, plant_status, created_time, update_time " +
            "FROM erp_base_approval WHERE plant_status = 1")
    List<ErpBaseApproval> selectAllActive();

    /**
     * 查询增量数据（根据更新时间筛选）
     */
    @Select("SELECT id, category, country, plant_no, plant_name, plant_status, created_time, update_time " +
            "FROM erp_base_approval WHERE plant_status = 1 AND update_time > #{lastSyncTime}")
    List<ErpBaseApproval> selectActiveAfter(@Param("lastSyncTime") LocalDateTime lastSyncTime);
}

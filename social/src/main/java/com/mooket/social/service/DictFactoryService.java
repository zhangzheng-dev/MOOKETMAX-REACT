package com.mooket.social.service;

import com.mooket.social.dto.FactoryFilterDTO;

/**
 * 厂号字典 Service
 */
public interface DictFactoryService {

    /**
     * 获取厂号筛选数据（按类别）
     * @param category 类别（牛/猪）
     * @return 筛选数据，包含国家列表和厂号列表
     */
    FactoryFilterDTO getFactoryFilter(String category);
}

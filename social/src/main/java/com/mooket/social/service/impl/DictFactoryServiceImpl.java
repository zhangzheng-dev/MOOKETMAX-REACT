package com.mooket.social.service.impl;

import com.mooket.social.dto.FactoryFilterDTO;
import com.mooket.social.entity.DictFactory;
import com.mooket.social.mapper.DictFactoryMapper;
import com.mooket.social.service.DictFactoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 厂号字典 Service 实现
 */
@Service
public class DictFactoryServiceImpl implements DictFactoryService {

    private final DictFactoryMapper dictFactoryMapper;

    public DictFactoryServiceImpl(DictFactoryMapper dictFactoryMapper) {
        this.dictFactoryMapper = dictFactoryMapper;
    }

    @Override
    public FactoryFilterDTO getFactoryFilter(String category) {
        // 使用单次查询获取所有数据，避免多次数据库往返
        List<DictFactory> factories = dictFactoryMapper.selectByCategory(category);

        // 提取国家列表（去重并保持顺序）
        List<String> countries = factories.stream()
                .map(DictFactory::getCountry)
                .distinct()
                .collect(Collectors.toList());

        // 构建厂号列表
        List<FactoryFilterDTO.FactoryItem> factoryItems = factories.stream()
                .map(f -> new FactoryFilterDTO.FactoryItem(f.getCountry(), f.getFactoryNo()))
                .collect(Collectors.toList());

        return new FactoryFilterDTO(countries, factoryItems);
    }
}

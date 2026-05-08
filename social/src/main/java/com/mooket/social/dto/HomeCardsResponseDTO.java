package com.mooket.social.dto;

import lombok.Data;

import java.util.List;

/**
 * 首页卡片响应DTO
 */
@Data
public class HomeCardsResponseDTO {
    /**
     * 卡片列表
     */
    private List<HomeCardItemDTO> cards;

    /**
     * 更新时间
     */
    private String updateTime;
}

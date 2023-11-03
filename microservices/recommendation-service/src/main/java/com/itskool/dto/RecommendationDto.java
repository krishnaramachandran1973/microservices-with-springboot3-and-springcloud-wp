package com.itskool.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder

public class RecommendationDto {
    private Long productId;
    private Long recommendationId;
    private String author;
    private int rate;
    private String content;
    private String serviceAddress;
}

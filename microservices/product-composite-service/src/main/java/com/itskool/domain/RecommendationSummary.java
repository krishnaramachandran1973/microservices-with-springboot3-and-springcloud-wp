package com.itskool.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class RecommendationSummary {
    private Long recommendationId;
    private String author;
    private int rate;
    private String content;
}

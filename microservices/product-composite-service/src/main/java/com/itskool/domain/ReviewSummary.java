package com.itskool.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class ReviewSummary {
    private Long reviewId;
    private String author;
    private String subject;
    private String content;
}

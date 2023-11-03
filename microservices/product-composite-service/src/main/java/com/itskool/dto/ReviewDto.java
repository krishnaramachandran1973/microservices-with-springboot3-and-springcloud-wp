package com.itskool.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class ReviewDto {
    private Long productId;
    private Long reviewId;
    private String author;
    private String subject;
    private String content;
    private String serviceAddress;
}

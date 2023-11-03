package com.itskool.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
@Document
@CompoundIndex(name = "prod-rec-id", unique = true, def = "{'productId':1,'recommendationId':1")
public class Recommendation {
    @Id
    private String id;

    private Long productId;
    private Long recommendationId;

    @Version
    private Integer version;

    private String author;
    private int rate;
    private String content;
}

package com.itskool.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class ServiceAddresses {
    private String compositeAddress;
    private String productAddress;
    private String reviewAddress;
    private String recommendationAddress;

}

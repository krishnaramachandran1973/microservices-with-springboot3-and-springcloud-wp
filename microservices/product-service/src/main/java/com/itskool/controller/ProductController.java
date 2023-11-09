package com.itskool.controller;

import com.itskool.dto.ProductDto;
import com.itskool.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;

    @GetMapping("/{productId}")
    public Mono<ProductDto> getProduct(@PathVariable Long productId, @RequestParam(value = "delay", required = false, defaultValue = "0") int delay,
                                       @RequestParam(value = "faultPercent", required = false, defaultValue = "0") int faultPercent) {
        return productService.getProductById(productId,delay,faultPercent);
    }
}

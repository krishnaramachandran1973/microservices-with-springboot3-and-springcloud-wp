package com.itskool.controller;

import com.itskool.dto.ProductDto;
import com.itskool.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;

    @GetMapping("/{productId}")
    public Mono<ProductDto> getProduct(@PathVariable Long productId) {
        return productService.getProductById(productId);
    }
}

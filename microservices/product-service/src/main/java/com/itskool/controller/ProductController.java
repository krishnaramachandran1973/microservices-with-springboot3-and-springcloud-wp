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
    public Mono<ProductDto> getProduct(@PathVariable Long productId) {
        return productService.getProductById(productId);
    }

    @PostMapping
    public Mono<ProductDto> createProduct(@RequestBody ProductDto productDto){
        return productService.createProduct(productDto);
    }

    @DeleteMapping("/{productId}")
    public Mono<Void> deleteProduct(@PathVariable Long productId){
        return productService.deleteProduct(productId);
    }
}

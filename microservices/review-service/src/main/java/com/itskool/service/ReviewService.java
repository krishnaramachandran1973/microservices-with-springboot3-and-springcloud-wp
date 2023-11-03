package com.itskool.service;

import com.itskool.domain.Review;
import com.itskool.dto.ReviewDto;
import com.itskool.exceptions.InvalidInputException;
import com.itskool.mapper.ReviewMapper;
import com.itskool.repository.ReviewRepository;
import com.itskool.util.ServiceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    @Qualifier("jdbcScheduler")
    private final Scheduler jdbcScheduler;
    private final ServiceUtil serviceUtil;

    public Mono<ReviewDto> createReview(ReviewDto reviewDto) {
        if (reviewDto.getProductId()<1){
            throw new InvalidInputException("Invalid productId: " + reviewDto.getProductId());
        }

        return Mono.fromCallable(() -> {
            Review reviewToCreate = reviewMapper.dtoToEntity(reviewDto);
            Review review = reviewRepository.save(reviewToCreate);
            return reviewMapper.entityToDto(review);
        }).onErrorMap(DataIntegrityViolationException.class,
                        ex-> new InvalidInputException("Duplicate key, Product Id: " + reviewDto.getProductId() + ", Review Id:" + reviewDto.getReviewId()))
                .subscribeOn(jdbcScheduler);
    }

    public Flux<ReviewDto> getReviews(Long productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        log.info("Will get reviews for product with id={}", productId);
        return Flux.fromIterable(reviewRepository.findReviewByProductId(productId))
                .map(review -> {
                    ReviewDto reviewDto = reviewMapper.entityToDto(review);
                    reviewDto.setServiceAddress(serviceUtil.getServiceAddress());
                    return reviewDto;
                })
                .subscribeOn(jdbcScheduler);
    }

    public Mono<Void> deleteReviews(Long productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        return Mono.fromRunnable(()-> reviewRepository.deleteAll(reviewRepository.findReviewByProductId(productId)))
                .subscribeOn(jdbcScheduler)
                .then();
    }
}

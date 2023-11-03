package com.itskool.service;

import com.itskool.domain.Recommendation;
import com.itskool.dto.RecommendationDto;
import com.itskool.exceptions.InvalidInputException;
import com.itskool.mapper.RecommendationMapper;
import com.itskool.repository.RecommendationRepository;
import com.itskool.util.ServiceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.util.logging.Level.FINE;

@Slf4j
@RequiredArgsConstructor
@Service
public class RecommendationService {
    private final RecommendationRepository recommendationRepository;
    private final RecommendationMapper recommendationMapper;
    private final ServiceUtil serviceUtil;

    public Mono<RecommendationDto> createRecommendation(RecommendationDto recommendationDto) {
        if (recommendationDto.getProductId()<1){
            throw new InvalidInputException("Invalid productId: " + recommendationDto.getProductId());
        }
        Recommendation recommendationToCreate = recommendationMapper.dtoToEntity(recommendationDto);
        return recommendationRepository.save(recommendationToCreate)
                .log(log.getName(), FINE)
                .onErrorMap(DuplicateKeyException.class,ex-> new InvalidInputException("Duplicate key, Product Id: " + recommendationDto.getProductId() + ", Recommendation Id:" + recommendationDto.getRecommendationId()))
                .map(recommendationMapper::entityToDto);
    }

    private RecommendationDto setServiceAddress(RecommendationDto e) {
        e.setServiceAddress(serviceUtil.getServiceAddress());
        return e;
    }

    public Flux<RecommendationDto> getRecommendations(Long productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        log.info("Will get recommendations for product with id={}", productId);

        return recommendationRepository.findRecommendationByProductId(productId)
                .log(log.getName(), FINE)
                .map(recommendationMapper::entityToDto)
                .map(this::setServiceAddress);
    }

    public Mono<Void> deleteRecommendations(Long productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        log.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}",
                productId);
        return recommendationRepository.deleteAll(recommendationRepository.findRecommendationByProductId(productId));
    }
}

package com.itskool.repository;

import com.itskool.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review,Long> {
    @Transactional(readOnly = true)
    List<Review> findReviewByProductId(Long productId);
}

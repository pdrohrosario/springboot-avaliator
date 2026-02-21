package com.project.feedbackservice.review.infrastruct.output.repository;

import com.project.feedbackservice.review.infrastruct.output.entities.JpaReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaReviewRepository extends JpaRepository<JpaReview, UUID> {
}

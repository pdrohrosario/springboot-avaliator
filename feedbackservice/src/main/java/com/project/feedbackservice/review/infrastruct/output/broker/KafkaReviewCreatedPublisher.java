package com.project.feedbackservice.review.infrastruct.output.broker;

import com.project.feedbackservice.review.application.ports.output.PublishReviewCreatedEvent;
import com.project.feedbackservice.review.domain.Review;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class KafkaReviewCreatedPublisher implements PublishReviewCreatedEvent {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public KafkaReviewCreatedPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${kafka.topic.review-created:review-created}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(Review review) {
        ReviewCreatedEvent event = new ReviewCreatedEvent(
                UUID.fromString(review.getId().toString()),
                UUID.fromString(review.getProductId().toString()),
                review.getRating(),
                review.getCreatedAt().atStartOfDay() // Convert LocalDate to LocalDateTime as expected by metricservice
        );

        // We use the productId as the Kafka message key to ensure ordered delivery per product
        kafkaTemplate.send(topic, event.productId().toString(), event);
    }
}

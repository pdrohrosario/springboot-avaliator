package com.project.metricservice.metrics.infrastruct.input.broker;

import com.project.metricservice.metrics.application.ports.input.ConsumeReviewCreated;
import com.project.metricservice.metrics.application.ports.input.ConsumeReviewCreatedInput;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class KafkaReviewCreatedListener {

    private final ConsumeReviewCreated consumeReviewCreated;

    public KafkaReviewCreatedListener(ConsumeReviewCreated consumeReviewCreated) {
        this.consumeReviewCreated = consumeReviewCreated;
    }

    @KafkaListener(topics = "${kafka.topic.review-created:review-created}", groupId = "${spring.kafka.consumer.group-id:metricservice}")
    public void listen(@Payload ReviewCreatedEvent event) {
        ConsumeReviewCreatedInput input = new ConsumeReviewCreatedInput(
                event.reviewId(),
                event.productId(),
                event.rating()
        );
        consumeReviewCreated.consume(input);
    }
}

package com.project.feedbackservice.review.infrastruct.input;

import com.project.feedbackservice.review.application.output.CreateReviewOutput;
import com.project.feedbackservice.review.application.ports.input.CreateReview;
import com.project.feedbackservice.review.infrastruct.input.request.CreateReviewRequest;
import com.project.feedbackservice.review.infrastruct.input.response.CreateReviewResponse;
import com.project.feedbackservice.review.infrastruct.mapper.ReviewControllerMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/review")
public class ReviewController {

    private final CreateReview createReview;

    public ReviewController(CreateReview createReview) {
        this.createReview = createReview;
    }

    @PostMapping("/create")
    public ResponseEntity<CreateReviewResponse> create(@Valid @RequestBody CreateReviewRequest request){
        CreateReviewOutput output = createReview.execute(ReviewControllerMapper.toInput(request));
        return new ResponseEntity<>(ReviewControllerMapper.toResponse(output), HttpStatus.CREATED);
    }
}

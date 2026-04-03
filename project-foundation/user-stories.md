# User Stories — Avaliator

## Epic 1: Product Management (`catalogservice`)

### US-01 Create product
As an operator, I want to create products so they can be listed and reviewed.

Acceptance criteria:
- Name is required, unique, max 50 chars.
- Price is required and non-negative.
- Category must be a valid enum value.
- Status defaults to `AVAILABLE`.

### US-02 Get product by ID
As a consumer/integrator, I want product details by ID.

Acceptance criteria:
- Invalid or missing ID returns informative error.
- Response includes core product fields.

### US-03 Search products
As a consumer, I want paginated search by name/description.

Acceptance criteria:
- Optional filters, pageable response, deterministic sorting.

## Epic 2: Product Reviews (`feedbackservice`)

### US-04 Create review
As a consumer, I want to submit a product review.

Acceptance criteria:
- Product must exist in catalogservice.
- Rating must be valid (1..5).
- Comment is required and limited to 500 chars.
- Review creation returns generated review identity and fields.

## Epic 3: Product Rating Metrics (`metricsservice` - planned)

### US-05 Update product metrics from review-created event
As a product analyst, I want metricsservice to update product rating metrics whenever a new review is created, so that product quality indicators are always available.

Acceptance criteria:
- Event processing is asynchronous and eventually consistent.
- Given a valid review-created event, product totalReviews increments by 1.
- Given a valid review-created event, averageRating is recalculated correctly.
- Given a valid review-created event, ratingDistribution bucket for the received rating (1..5) increments by 1.
- Given a duplicate event for the same reviewId, no metric is changed again (idempotency).
- Given temporary processing failure, event reprocessing preserves data correctness (no double count).

### US-06 Query product rating metrics
As an internal consumer, I want to query consolidated rating metrics by product, so that I can present product reputation data.

Acceptance criteria:
- Response includes at least: productId, totalReviews, averageRating, ratingDistribution.
- Products without reviews return an explicit "no reviews yet" business state.
- Returned data may reflect eventual consistency relative to recently created reviews.

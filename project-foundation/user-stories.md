# User Stories — Avaliator

## Epic 1: Product Management (`catalogservice`)

### US-01 Create product
As an operator, I want to create products so they can be listed and reviewed.

Acceptance criteria:
- Name is required, unique, max 50 chars.
- Price is required and strictly positive (> 0.00). Free products are not allowed in the system.
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

### US-07 Update product
As an operator, I want to update product details so that catalog information remains accurate.

Acceptance criteria:
- Product ID must be a valid UUID in the domain representation (`ProductId` type, not `Long` type).
- Product name can be updated but uniqueness must be preserved (if name is changed, it must not clash with another existing product's name).
- Product price can be updated, and must remain strictly positive (> 0.00).
- Product status and category can be updated but must follow valid enum invariants.
- Product description can be updated or set to null.
- If the product ID is not found, throw a `ProductNotFound` domain exception (mapped to `404 Not Found` at the API boundary).
- The use case must be fully implemented, wired through the `UpdateProduct` input port, and exposed via `PUT /product/update` endpoint.

## Epic 2: Product Reviews (`feedbackservice`)

### US-04 Create review
As a consumer, I want to submit a product review.

Acceptance criteria:
- Product must exist in catalogservice (validated synchronously via Feign client).
- Rating must be valid (1..5).
- Comment is required, non-blank, and limited to 500 chars. Must be validated at the API boundary using `@NotBlank` and `@Size(max = 500)` on the request object.
- Review creation returns generated review identity and fields.
- If the product ID is malformed, invalid, or null, the API must return a clear `400 Bad Request` with an informative error message (handled by mapping `ProductIdIsNotValidException` to HTTP 400).
- If catalogservice is unreachable or returns a 5xx error, feedbackservice must re-throw it as a clean `ApiIntegrationException` (mapped to `503 Service Unavailable`) rather than exposing internal Hibernate integration or Feign decoding errors.
- The `Review` aggregate root must expose no public setters; any domain mutations must go through validated domain methods.

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
- *Implementation Note*: The database migrations (`V1__create_product_metric_table.sql`, `V2__create_processed_review_event_table.sql`) are scaffolded, but the service is in early scaffolding state and production Java code is planned for development.

### US-06 Query product rating metrics
As an internal consumer, I want to query consolidated rating metrics by product, so that I can present product reputation data.

Acceptance criteria:
- Response includes at least: productId, totalReviews, averageRating, ratingDistribution.
- Products without reviews return an explicit "no reviews yet" business state.
- Returned data may reflect eventual consistency relative to recently created reviews.

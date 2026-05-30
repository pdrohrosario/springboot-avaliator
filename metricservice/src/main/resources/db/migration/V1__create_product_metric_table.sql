CREATE SCHEMA IF NOT EXISTS metric_schema;

CREATE TABLE IF NOT EXISTS metric_schema.product_metric (
    product_id      UUID            PRIMARY KEY,
    total_reviews   BIGINT          NOT NULL DEFAULT 0,
    rating_sum      BIGINT          NOT NULL DEFAULT 0,
    average_rating  NUMERIC(10,2)   NOT NULL DEFAULT 0,
    rating_1_count  BIGINT          NOT NULL DEFAULT 0,
    rating_2_count  BIGINT          NOT NULL DEFAULT 0,
    rating_3_count  BIGINT          NOT NULL DEFAULT 0,
    rating_4_count  BIGINT          NOT NULL DEFAULT 0,
    rating_5_count  BIGINT          NOT NULL DEFAULT 0,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_product_metric_total_reviews_non_negative CHECK (total_reviews >= 0),
    CONSTRAINT ck_product_metric_rating_sum_non_negative CHECK (rating_sum >= 0),
    CONSTRAINT ck_product_metric_average_rating_range CHECK (average_rating >= 0 AND average_rating <= 5),
    CONSTRAINT ck_product_metric_rating_1_non_negative CHECK (rating_1_count >= 0),
    CONSTRAINT ck_product_metric_rating_2_non_negative CHECK (rating_2_count >= 0),
    CONSTRAINT ck_product_metric_rating_3_non_negative CHECK (rating_3_count >= 0),
    CONSTRAINT ck_product_metric_rating_4_non_negative CHECK (rating_4_count >= 0),
    CONSTRAINT ck_product_metric_rating_5_non_negative CHECK (rating_5_count >= 0)
);
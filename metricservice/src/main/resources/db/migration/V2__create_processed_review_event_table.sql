CREATE TABLE IF NOT EXISTS metric_schema.processed_review_event (
    review_id       UUID        PRIMARY KEY,
    event_id        UUID        UNIQUE,
    processed_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_processed_review_event_processed_at
    ON metric_schema.processed_review_event (processed_at);

CREATE TABLE review (
  id UUID PRIMARY KEY,
  product_id UUID NOT NULL,
  rating SMALLINT NOT NULL,
  comment TEXT,
  created_at DATE DEFAULT CURRENT_DATE
);
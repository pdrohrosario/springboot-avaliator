package com.project.metricservice.infrastruct;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FlywayMigrationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldCreateMetricTablesByFlywayMigrations() {
                assertThat(countSchema("METRIC_SCHEMA")).isEqualTo(1);
        assertThat(countTable("PRODUCT_METRIC")).isEqualTo(1);
        assertThat(countTable("PROCESSED_REVIEW_EVENT")).isEqualTo(1);
    }

        @Test
        void shouldCreateExpectedColumnsForProductMetricTable() {
                assertThat(countColumn("PRODUCT_METRIC", "PRODUCT_ID")).isEqualTo(1);
                assertThat(countColumn("PRODUCT_METRIC", "TOTAL_REVIEWS")).isEqualTo(1);
                assertThat(countColumn("PRODUCT_METRIC", "RATING_SUM")).isEqualTo(1);
                assertThat(countColumn("PRODUCT_METRIC", "AVERAGE_RATING")).isEqualTo(1);
                assertThat(countColumn("PRODUCT_METRIC", "RATING_1_COUNT")).isEqualTo(1);
                assertThat(countColumn("PRODUCT_METRIC", "RATING_2_COUNT")).isEqualTo(1);
                assertThat(countColumn("PRODUCT_METRIC", "RATING_3_COUNT")).isEqualTo(1);
                assertThat(countColumn("PRODUCT_METRIC", "RATING_4_COUNT")).isEqualTo(1);
                assertThat(countColumn("PRODUCT_METRIC", "RATING_5_COUNT")).isEqualTo(1);
                assertThat(countColumn("PRODUCT_METRIC", "UPDATED_AT")).isEqualTo(1);
        }

        @Test
        void shouldCreateExpectedColumnsForProcessedReviewEventTable() {
                assertThat(countColumn("PROCESSED_REVIEW_EVENT", "REVIEW_ID")).isEqualTo(1);
                assertThat(countColumn("PROCESSED_REVIEW_EVENT", "EVENT_ID")).isEqualTo(1);
                assertThat(countColumn("PROCESSED_REVIEW_EVENT", "PROCESSED_AT")).isEqualTo(1);
        }

    @Test
    void shouldApplyDefaultValuesOnProductMetricInsert() {
        UUID productId = UUID.randomUUID();

        jdbcTemplate.update("""
                INSERT INTO metric_schema.product_metric (product_id)
                VALUES (?)
                """, productId);

        Map<String, Object> row = jdbcTemplate.queryForMap("""
                SELECT total_reviews, rating_sum, average_rating,
                       rating_1_count, rating_2_count, rating_3_count, rating_4_count, rating_5_count,
                       updated_at
                FROM metric_schema.product_metric
                WHERE product_id = ?
                """, productId);

        assertThat(((Number) row.get("TOTAL_REVIEWS")).longValue()).isZero();
        assertThat(((Number) row.get("RATING_SUM")).longValue()).isZero();
        assertThat((BigDecimal) row.get("AVERAGE_RATING")).isEqualByComparingTo("0.00");
        assertThat(((Number) row.get("RATING_1_COUNT")).longValue()).isZero();
        assertThat(((Number) row.get("RATING_2_COUNT")).longValue()).isZero();
        assertThat(((Number) row.get("RATING_3_COUNT")).longValue()).isZero();
        assertThat(((Number) row.get("RATING_4_COUNT")).longValue()).isZero();
        assertThat(((Number) row.get("RATING_5_COUNT")).longValue()).isZero();
        assertThat(row.get("UPDATED_AT")).isNotNull();
    }

    @Test
    void shouldAllowNullEventIdAndSetProcessedAtDefault() {
        UUID reviewId = UUID.randomUUID();

        jdbcTemplate.update("""
                INSERT INTO metric_schema.processed_review_event (review_id)
                VALUES (?)
                """, reviewId);

        Map<String, Object> row = jdbcTemplate.queryForMap("""
                SELECT event_id, processed_at
                FROM metric_schema.processed_review_event
                WHERE review_id = ?
                """, reviewId);

        assertThat(row.get("EVENT_ID")).isNull();
        assertThat(row.get("PROCESSED_AT")).isInstanceOf(Timestamp.class);
    }

    @Test
    void shouldRejectNegativeTotalsByCheckConstraint() {
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO metric_schema.product_metric (product_id, total_reviews)
                VALUES (?, ?)
                """, UUID.randomUUID(), -1L))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

        @Test
        void shouldRejectAverageRatingGreaterThanFive() {
                assertThatThrownBy(() -> jdbcTemplate.update("""
                                INSERT INTO metric_schema.product_metric (product_id, average_rating)
                                VALUES (?, ?)
                                """, UUID.randomUUID(), BigDecimal.valueOf(5.01)))
                                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void shouldRejectNegativeRatingDistributionBucket() {
                assertThatThrownBy(() -> jdbcTemplate.update("""
                                INSERT INTO metric_schema.product_metric (product_id, rating_3_count)
                                VALUES (?, ?)
                                """, UUID.randomUUID(), -1L))
                                .isInstanceOf(DataIntegrityViolationException.class);
        }

    @Test
    void shouldRejectDuplicateReviewIdInProcessedReviewEvent() {
        UUID reviewId = UUID.randomUUID();

        jdbcTemplate.update("""
                INSERT INTO metric_schema.processed_review_event (review_id, event_id)
                VALUES (?, ?)
                """, reviewId, UUID.randomUUID());

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO metric_schema.processed_review_event (review_id, event_id)
                VALUES (?, ?)
                """, reviewId, UUID.randomUUID()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldRejectDuplicateEventIdWhenProvided() {
        UUID eventId = UUID.randomUUID();

        jdbcTemplate.update("""
                INSERT INTO metric_schema.processed_review_event (review_id, event_id)
                VALUES (?, ?)
                """, UUID.randomUUID(), eventId);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO metric_schema.processed_review_event (review_id, event_id)
                VALUES (?, ?)
                """, UUID.randomUUID(), eventId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private int countTable(String tableName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.TABLES
                WHERE TABLE_SCHEMA = 'METRIC_SCHEMA' AND TABLE_NAME = ?
                """, Integer.class, tableName);
        return count == null ? 0 : count;
    }

    private int countSchema(String schemaName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.SCHEMATA
                WHERE SCHEMA_NAME = ?
                """, Integer.class, schemaName);
        return count == null ? 0 : count;
    }

    private int countColumn(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = 'METRIC_SCHEMA'
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = ?
                """, Integer.class, tableName, columnName);
        return count == null ? 0 : count;
    }
}

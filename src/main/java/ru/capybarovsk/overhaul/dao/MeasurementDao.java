package ru.capybarovsk.overhaul.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.capybarovsk.overhaul.model.Measurement;

@Repository
public class MeasurementDao {
    // https://www.postgresql.org/docs/current/datatype-datetime.html#DATATYPE-INTERVAL-INPUT
    private static final String RATE_LIMIT_WINDOW = "1 hour";
    private static final long RATE_LIMIT_MAX = 12;

    private final JdbcTemplate jdbcTemplate;

    public MeasurementDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Measurement> getUserMeasurements(long userId) {
        return getUserMeasurements(userId, 100);
    }

    public List<Measurement> getUserMeasurements(long userId, int limit) {
        return jdbcTemplate.query(
                String.format(
                        "SELECT id, measurement, created_at FROM measurements WHERE account_id = %d " +
                                "ORDER BY created_at DESC LIMIT %d",
                        userId,
                        limit
                ),
                (rs, rowId) -> mapMeasurement(rs)
        );
    }

    public boolean hasTooFrequentMeasurements() {
        return Objects.requireNonNull(
                jdbcTemplate.queryForObject(
                        String.format(
                                "SELECT COUNT(*) FROM measurements WHERE created_at > now() - interval '%s'",
                                RATE_LIMIT_WINDOW
                        ),
                        Long.class
                )
        ) >= RATE_LIMIT_MAX;
    }

    private Measurement mapMeasurement(ResultSet rs) throws SQLException {
        return new Measurement(
                rs.getLong(1),
                rs.getString(2),
                rs.getTimestamp(3)
        );
    }

    public void create(long id, String measurement) {
        jdbcTemplate.update(
                String.format(
                        "INSERT INTO measurements (account_id, measurement) VALUES (%d, '%s')",
                        id,
                        measurement
                )
        );
    }
}

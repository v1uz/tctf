package ru.capybarovsk.overhaul.dao;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.capybarovsk.overhaul.model.Invoice;

@Repository
public class InvoiceDao {
    public final static BigDecimal TARIFF = new BigDecimal("35.74"); // ₡ per m³

    private final JdbcTemplate jdbcTemplate;

    public InvoiceDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Invoice> getUserInvoices(long userId) {
        return jdbcTemplate.query(
                String.format(
                        "SELECT id, total, paid, created_at FROM invoices WHERE account_id = %d " +
                                "ORDER BY created_at DESC LIMIT 100",
                        userId
                ),
                (rs, rowId) -> mapInvoice(rs)
        );
    }

    private Invoice mapInvoice(ResultSet rs) throws SQLException {
        return new Invoice(
                rs.getLong(1),
                rs.getBigDecimal(2),
                rs.getBoolean(3),
                rs.getTimestamp(4)
        );
    }

    public void create(long userId, BigDecimal total) {
        jdbcTemplate.update(
                "INSERT INTO invoices (account_id, total) VALUES (?, ?)",
                userId,
                total
        );
    }
}

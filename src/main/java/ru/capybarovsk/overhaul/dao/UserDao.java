package ru.capybarovsk.overhaul.dao;

import jakarta.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.capybarovsk.overhaul.model.User;

@Repository
public class UserDao {
    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Nullable
    public User getUserById(long userId) {
        return DataAccessUtils.singleResult(
                jdbcTemplate.query(
                        String.format(
                            "SELECT id, login, password, name, address, tariff FROM accounts WHERE id = %d",
                            userId
                        ),
                        (rs, rowNum) -> mapUser(rs)
                )
        );
    }

    @Nullable
    public User getUserByLogin(String login) {
        return DataAccessUtils.singleResult(
                jdbcTemplate.query(
                        "SELECT id, login, password, name, address, tariff FROM accounts WHERE login = ?",
                        (rs, rowNum) -> mapUser(rs),
                        login
                )
        );
    }

    public long createUser(String login, String password, String clientName, String clientAddress) {
        return Objects.requireNonNull(
                jdbcTemplate.queryForObject(
                        "INSERT INTO accounts (login, password, name, address, tariff) VALUES" +
                                "(?, ?, ?, ?, ?) ON CONFLICT DO NOTHING RETURNING (id)",
                        Long.class,
                        login,
                        password,
                        clientName,
                        clientAddress,
                        InvoiceDao.TARIFF
                )
        );
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                rs.getString(5),
                rs.getBigDecimal(6)
        );
    }
}

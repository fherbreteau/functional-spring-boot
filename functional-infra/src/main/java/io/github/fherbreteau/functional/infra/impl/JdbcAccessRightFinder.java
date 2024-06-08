package io.github.fherbreteau.functional.infra.impl;

import io.github.fherbreteau.functional.domain.entities.AccessRight;
import io.github.fherbreteau.functional.infra.AccessRightFinder;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static io.github.fherbreteau.functional.infra.mapper.ItemAccessSQLConstants.*;

@Transactional
public class JdbcAccessRightFinder implements AccessRightFinder {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcAccessRightFinder(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public AccessRight getAccess(UUID itemId, String attribution) {
        String query = """
                SELECT type
                FROM item_access
                WHERE item_id = :item_id
                AND attribution = :attribution
                AND value = TRUE;
                """;
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(COL_ITEM_ID, itemId)
                .addValue(COL_ATTRIBUTION, attribution);
        List<String> accessTypes = jdbcTemplate.query(query, params, new SingleColumnRowMapper<>());
        AccessRight right = AccessRight.none();
        if (accessTypes.contains(TYPE_READ)) {
            right = right.addRead();
        }
        if (accessTypes.contains(TYPE_WRITE)) {
            right = right.addWrite();
        }
        if (accessTypes.contains(TYPE_EXECUTE)) {
            right = right.addExecute();
        }
        return right;
    }
}

package com.example.Liderum.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs only from the PostgreSQL CI job, against its disposable service database.
 * The database URL and credentials are supplied by that job, never by production.
 */
@SpringBootTest(properties = {
        "jwt.secret=test_only_postgresql_ci_secret_32_bytes_minimum",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@ActiveProfiles("prod")
class PostgreSqlFlywaySchemaCiIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void appliesV1AndHibernateValidatesThePostgresqlSchema() {
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '1' AND success = true", Integer.class))
                .isEqualTo(1);

        for (String table : new String[] {"guilds", "users", "member", "team", "event", "attendance"}) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables "
                            + "WHERE table_schema = 'public' AND table_name = ?", Integer.class, table);
            assertThat(count).as("table %s", table).isEqualTo(1);
        }

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.table_constraints "
                        + "WHERE table_schema = 'public' AND constraint_type = 'FOREIGN KEY'", Integer.class))
                .isGreaterThanOrEqualTo(8);

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pg_indexes WHERE schemaname = 'public' "
                        + "AND indexname IN ('idx_users_guild', 'idx_member_guild', 'idx_team_guild', "
                        + "'idx_event_guild', 'idx_attendance_member', 'idx_attendance_event')", Integer.class))
                .isEqualTo(6);
    }
}

package com.example.Liderum.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "jwt.secret=test_only_flyway_schema_secret_32_bytes_minimum",
        "spring.datasource.url=jdbc:h2:mem:flyway_schema;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@ActiveProfiles("default")
class FlywaySchemaIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void appliesAllMigrationsAndCreatesExpectedSchemaWithoutHibernateUpdate() {
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM \"flyway_schema_history\" WHERE \"version\" IN ('1', '2', '3', '4') AND \"success\" = TRUE", Integer.class))
                .isEqualTo(4);

        for (String table : new String[] {"GUILDS", "USERS", "MEMBER", "TEAM", "EVENT", "ATTENDANCE", "USER_ACTIVATION_TOKENS"}) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME = ?", Integer.class, table);
            assertThat(count).as("table %s", table).isEqualTo(1);
        }

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME = 'flyway_schema_history'", Integer.class))
                .isEqualTo(1);

        assertNotNullable("USERS", "GUILD_ID");
        assertNotNullable("USERS", "GUILD_ROLE");
        assertNotNullable("EVENT", "NAME");
        assertNotNullable("EVENT", "DATE");
        assertNotNullable("ATTENDANCE", "STATUS");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA = 'PUBLIC' "
                        + "AND TABLE_NAME = 'ATTENDANCE' AND CONSTRAINT_NAME = 'UK_ATTENDANCE_MEMBER_EVENT' "
                        + "AND CONSTRAINT_TYPE = 'UNIQUE'", Integer.class)).isEqualTo(1);
        assertNotNullable("USERS", "STATUS");
        assertNullable("USERS", "PASSWORD");
        assertNotNullable("USERS", "CREDENTIAL_VERSION");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COLUMN_DEFAULT FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='PUBLIC' AND TABLE_NAME='USERS' AND COLUMN_NAME='CREDENTIAL_VERSION'", String.class))
                .contains("0");
    }

    @Test
    @Transactional
    void rejectsNullValuesForNewlyRequiredColumns() {
        jdbcTemplate.update("INSERT INTO guilds (name) VALUES ('constraint-test-guild')");
        Long guildId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM guilds", Long.class);

        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO users (username, email, password, guild_role, status) VALUES (?, ?, ?, ?, ?)",
                "missing-guild", "missing-guild@example.test", "unused", "MARECHAL", "ACTIVE"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO users (username, email, password, guild_id, status) VALUES (?, ?, ?, ?, ?)",
                "missing-role", "missing-role@example.test", "unused", guildId, "ACTIVE"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO event (date, guild_id) VALUES (?, ?)", Timestamp.valueOf(LocalDateTime.parse("2026-01-01T10:00:00")), guildId))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO event (name, guild_id) VALUES (?, ?)", "missing-date", guildId))
                .isInstanceOf(DataIntegrityViolationException.class);

        jdbcTemplate.update("INSERT INTO event (name, date, guild_id) VALUES (?, ?, ?)",
                "constraint-event", Timestamp.valueOf(LocalDateTime.parse("2026-01-01T10:00:00")), guildId);
        jdbcTemplate.update("INSERT INTO member (guild_id) VALUES (?)", guildId);
        Long eventId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM event", Long.class);
        Long memberId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM member", Long.class);
        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO attendance (member_id, event_id) VALUES (?, ?)", memberId, eventId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    void rejectsDuplicateAttendanceForTheSameMemberAndEvent() {
        jdbcTemplate.update("INSERT INTO guilds (name) VALUES ('attendance-unique-test')");
        Long guildId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM guilds", Long.class);
        jdbcTemplate.update("INSERT INTO event (name, date, guild_id) VALUES (?, ?, ?)",
                "unique-event", Timestamp.valueOf(LocalDateTime.parse("2026-01-01T10:00:00")), guildId);
        jdbcTemplate.update("INSERT INTO member (guild_id) VALUES (?)", guildId);
        Long eventId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM event", Long.class);
        Long memberId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM member", Long.class);
        jdbcTemplate.update("INSERT INTO attendance (member_id, event_id, status) VALUES (?, ?, ?)", memberId, eventId, "PRESENTE");

        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO attendance (member_id, event_id, status) VALUES (?, ?, ?)", memberId, eventId, "FALTOU"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private void assertNotNullable(String table, String column) {
        assertThat(jdbcTemplate.queryForObject(
                "SELECT IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'PUBLIC' "
                        + "AND TABLE_NAME = ? AND COLUMN_NAME = ?", String.class, table, column))
                .as("%s.%s is not nullable", table, column).isEqualTo("NO");
    }

    private void assertNullable(String table, String column) {
        assertThat(jdbcTemplate.queryForObject(
                "SELECT IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'PUBLIC' "
                        + "AND TABLE_NAME = ? AND COLUMN_NAME = ?", String.class, table, column))
                .as("%s.%s remains nullable", table, column).isEqualTo("YES");
    }
}

package com.example.Liderum.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

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
    void appliesVersionOneAndCreatesExpectedSchemaWithoutHibernateUpdate() {
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM \"flyway_schema_history\" WHERE \"version\" = '1'", Integer.class))
                .isEqualTo(1);

        for (String table : new String[] {"GUILDS", "USERS", "MEMBER", "TEAM", "EVENT", "ATTENDANCE"}) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME = ?", Integer.class, table);
            assertThat(count).as("table %s", table).isEqualTo(1);
        }

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME = 'flyway_schema_history'", Integer.class))
                .isEqualTo(1);
    }
}

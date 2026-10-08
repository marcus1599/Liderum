package com.example.Liderum.config;

import com.example.Liderum.LiderumApplication;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs against the disposable PostgreSQL service in the postgres-flyway CI job.
 * It creates and drops only a uniquely named schema inside that disposable database.
 */
class PostgreSqlFlywayUpgradeCiIT {

    private static final String LEGACY_USERNAME = "legacy_activation_upgrade_user";
    private static final String LEGACY_PASSWORD = "legacy-test-password-only";

    @Test
    void upgradesV1WithExistingUserToV2AndStartsHibernateValidatedContext() throws Exception {
        String baseUrl = requiredEnv("DB_URL");
        String username = requiredEnv("DB_USERNAME");
        String password = requiredEnv("DB_PASSWORD");
        String schema = "activation_upgrade_" + UUID.randomUUID().toString().replace("-", "");
        String schemaUrl = withCurrentSchema(baseUrl, schema);

        try {
            createSchema(baseUrl, username, password, schema);

            Flyway.configure()
                    .dataSource(schemaUrl, username, password)
                    .schemas(schema)
                    .defaultSchema(schema)
                    .locations("classpath:db/migration")
                    .target("1")
                    .load()
                    .migrate();

            DataSource schemaDataSource = dataSource(schemaUrl, username, password);
            JdbcTemplate jdbc = new JdbcTemplate(schemaDataSource);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM flyway_schema_history WHERE version = '1' AND success", Integer.class))
                    .isEqualTo(1);

            String legacyHash = new BCryptPasswordEncoder().encode(LEGACY_PASSWORD);
            Long guildId = jdbc.queryForObject(
                    "INSERT INTO guilds (name, server_name) VALUES (?, ?) RETURNING id",
                    Long.class, "Legacy upgrade guild", "Legacy server");
            Long userId = jdbc.queryForObject(
                    "INSERT INTO users (username, email, password, guild_role, guild_id) VALUES (?, ?, ?, ?, ?) RETURNING id",
                    Long.class, LEGACY_USERNAME, "legacy-upgrade@example.test", legacyHash, "MARECHAL", guildId);

            Flyway.configure()
                    .dataSource(schemaUrl, username, password)
                    .schemas(schema)
                    .defaultSchema(schema)
                    .locations("classpath:db/migration")
                    .load()
                    .migrate();

            assertThat(jdbc.queryForList(
                    "SELECT version FROM flyway_schema_history WHERE success ORDER BY installed_rank", String.class))
                    .containsExactly("1", "2");
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE id = ?", Integer.class, userId)).isEqualTo(1);
            assertThat(jdbc.queryForObject("SELECT guild_id FROM users WHERE id = ?", Long.class, userId)).isEqualTo(guildId);
            assertThat(jdbc.queryForObject("SELECT password FROM users WHERE id = ?", String.class, userId)).isEqualTo(legacyHash);
            assertThat(new BCryptPasswordEncoder().matches(LEGACY_PASSWORD,
                    jdbc.queryForObject("SELECT password FROM users WHERE id = ?", String.class, userId))).isTrue();
            assertThat(jdbc.queryForObject("SELECT status FROM users WHERE id = ?", String.class, userId)).isEqualTo("ACTIVE");
            assertThat(jdbc.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = ? AND table_name = 'user_activation_tokens'",
                    Integer.class, schema)).isEqualTo(1);
            assertThat(jdbc.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.table_constraints WHERE constraint_schema = ? "
                            + "AND table_name = 'user_activation_tokens' AND constraint_name = 'fk_activation_user'",
                    Integer.class, schema)).isEqualTo(1);
            assertThat(jdbc.queryForObject(
                    "SELECT COUNT(*) FROM pg_indexes WHERE schemaname = ? AND indexname = 'idx_activation_token_user'",
                    Integer.class, schema)).isEqualTo(1);

            try (ConfigurableApplicationContext context = new SpringApplicationBuilder(LiderumApplication.class)
                    .web(WebApplicationType.SERVLET)
                    .properties(Map.of("spring.main.banner-mode", "off"))
                    .run(
                            "--spring.profiles.active=prod",
                            "--server.port=0",
                            "--spring.datasource.url=" + schemaUrl,
                            "--spring.datasource.username=" + username,
                            "--spring.datasource.password=" + password,
                            "--spring.flyway.schemas=" + schema,
                            "--spring.flyway.default-schema=" + schema,
                            "--spring.jpa.hibernate.ddl-auto=validate",
                            "--spring.rabbitmq.listener.simple.auto-startup=false",
                            "--jwt.secret=test_only_postgresql_upgrade_secret_32_bytes_minimum")) {
                assertThat(context.getBean("entityManagerFactory")).isNotNull();
                Authentication authentication = context.getBean(AuthenticationManager.class)
                        .authenticate(new UsernamePasswordAuthenticationToken(LEGACY_USERNAME, LEGACY_PASSWORD));
                assertThat(authentication.isAuthenticated()).isTrue();
            }
        } finally {
            dropSchema(baseUrl, username, password, schema);
        }
    }

    private static void createSchema(String url, String username, String password, String schema) throws Exception {
        try (Connection connection = dataSource(url, username, password).getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA \"" + schema + "\"");
        }
    }

    private static void dropSchema(String url, String username, String password, String schema) throws Exception {
        try (Connection connection = dataSource(url, username, password).getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP SCHEMA IF EXISTS \"" + schema + "\" CASCADE");
        }
    }

    private static DataSource dataSource(String url, String username, String password) {
        return new DriverManagerDataSource(url, username, password);
    }

    private static String withCurrentSchema(String url, String schema) {
        return url + (url.contains("?") ? "&" : "?") + "currentSchema=" + schema;
    }

    private static String requiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " must be set by the disposable PostgreSQL CI job");
        }
        return value;
    }
}

package com.smartreminder.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Database Configuration for Render & Local Environments
 * 
 * Supports both:
 * 1. Render Managed PostgreSQL (automatically parses Render's 'DATABASE_URL'
 *    format e.g. postgres://user:pass@host:5432/db into a valid JDBC PostgreSQL connection).
 * 2. Standard Spring Boot DataSource properties (SPRING_DATASOURCE_URL, username, password).
 */
@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${DATABASE_URL:#{null}}")
    private String databaseUrl;

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        String dbUrl = (databaseUrl != null && !databaseUrl.trim().isEmpty())
                ? databaseUrl.trim()
                : System.getenv("DATABASE_URL");

        // If Render provides DATABASE_URL in postgres:// or postgresql:// format without jdbc: prefix
        if (dbUrl != null && !dbUrl.trim().isEmpty() && !dbUrl.startsWith("jdbc:")) {
            try {
                log.info("Render DATABASE_URL detected. Converting to PostgreSQL JDBC format...");
                URI uri = new URI(dbUrl);

                String username = null;
                String password = null;
                if (uri.getUserInfo() != null) {
                    String[] userInfo = uri.getUserInfo().split(":", 2);
                    username = userInfo[0];
                    if (userInfo.length > 1) {
                        password = userInfo[1];
                    }
                }

                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String host = uri.getHost();
                String path = uri.getPath(); // e.g. /smart_reminder_db

                StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://")
                        .append(host)
                        .append(":")
                        .append(port)
                        .append(path);

                // For cloud databases (non-localhost), ensure SSL requirement is configured
                boolean isLocal = "localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host);
                if (uri.getQuery() != null && !uri.getQuery().isEmpty()) {
                    jdbcUrl.append("?").append(uri.getQuery());
                } else if (!isLocal) {
                    jdbcUrl.append("?sslmode=require");
                }

                log.info("Constructed PostgreSQL JDBC URL for host: {}:{}", host, port);

                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(jdbcUrl.toString());
                if (username != null) {
                    config.setUsername(username);
                }
                if (password != null) {
                    config.setPassword(password);
                }
                config.setDriverClassName("org.postgresql.Driver");
                config.setMaximumPoolSize(5);
                config.setMinimumIdle(2);
                config.setIdleTimeout(300000);
                config.setMaxLifetime(600000);
                config.setConnectionTimeout(30000);

                return new HikariDataSource(config);
            } catch (Exception ex) {
                log.error("Failed to parse DATABASE_URL ({}): {}. Falling back to default datasource properties.",
                        dbUrl, ex.getMessage());
            }
        }

        // Fallback to standard Spring Boot datasource properties (SPRING_DATASOURCE_URL or application.properties)
        return properties.initializeDataSourceBuilder().build();
    }
}

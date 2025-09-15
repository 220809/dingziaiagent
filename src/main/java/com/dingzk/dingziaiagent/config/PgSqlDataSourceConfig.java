package com.dingzk.dingziaiagent.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.datasource.postgresql")
public class PgSqlDataSourceConfig {

    private String driverClassName;
    private String url;
    private String username;
    private String password;

    @Bean("pgsqlDataSource")
    public DataSource pgsqlDataSource() {
        return DataSourceBuilder.create()
                .driverClassName(driverClassName)
                .url(url)
                .username(username)
                .password(password)
                .build();
    }

    @Bean("pgsqlJdbcTemplate")
    public JdbcTemplate pgsqlJdbcTemplate(@Qualifier("pgsqlDataSource") DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.setDataSource(dataSource);
        return jdbcTemplate;
    }
}

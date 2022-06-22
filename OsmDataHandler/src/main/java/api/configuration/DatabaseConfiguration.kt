package api.configuration

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

@Configuration
open class DatabaseConfiguration {

    @Bean
    open fun datasource(): HikariDataSource {
        val config = HikariConfig();
        config.driverClassName = "org.postgresql.Driver"
        config.username = "postgres"
        config.password = "postgres"
        config.schema = "diplom"
        config.jdbcUrl = "jdbc:postgresql://localhost:5432/postgres"
        config.maximumPoolSize = 15
        config.addDataSourceProperty("cachePrepStmts", "true")
        config.addDataSourceProperty("prepStmtCacheSize", "250")
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        config.connectionTimeout = 100000

        return HikariDataSource(config)
    }

    @Bean
    open fun jdbcTemplate(): JdbcTemplate {
        return JdbcTemplate(datasource())
    }

    @Bean
    open fun namedJdbcTemplate(): NamedParameterJdbcTemplate {
        return NamedParameterJdbcTemplate(datasource())
    }
}
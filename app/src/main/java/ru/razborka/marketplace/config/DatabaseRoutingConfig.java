package ru.razborka.marketplace.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(DatabaseRoutingConfig.RoutingDataSourceProperties.class)
public class DatabaseRoutingConfig {

    @Bean(name = "writeDataSource")
    public DataSource writeDataSource(RoutingDataSourceProperties props) {
        return createDataSource(props.write());
    }

    @Bean(name = "readDataSource")
    public DataSource readDataSource(RoutingDataSourceProperties props,
            @Qualifier("writeDataSource") DataSource writeDataSource) {
        DataSource replica = createDataSource(props.read());
        return new FallbackDataSource(replica, writeDataSource);
    }

    @Primary
    @Bean
    public DataSource dataSource(@Qualifier("writeDataSource") DataSource writeDataSource,
            @Qualifier("readDataSource") DataSource readDataSource) {
        ReadWriteRoutingDataSource routing = new ReadWriteRoutingDataSource();
        Map<Object, Object> targets = new HashMap<>();
        targets.put(RoutingKey.WRITE, writeDataSource);
        targets.put(RoutingKey.READ, readDataSource);
        routing.setTargetDataSources(targets);
        routing.setDefaultTargetDataSource(writeDataSource);
        routing.afterPropertiesSet();
        return new LazyConnectionDataSourceProxy(routing);
    }

    private static DataSource createDataSource(PoolProperties props) {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(props.url());
        cfg.setUsername(props.username());
        cfg.setPassword(props.password());
        cfg.setMaximumPoolSize(props.maxPoolSize());
        cfg.setMinimumIdle(props.minIdle());
        cfg.setPoolName(props.poolName());
        cfg.setInitializationFailTimeout(-1);
        return new HikariDataSource(cfg);
    }

    private enum RoutingKey {
        WRITE,
        READ
    }

    private static class ReadWriteRoutingDataSource extends AbstractRoutingDataSource {
        @Override
        protected Object determineCurrentLookupKey() {
            if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
                return RoutingKey.READ;
            }
            return RoutingKey.WRITE;
        }
    }

    @ConfigurationProperties(prefix = "app.datasource")
    public record RoutingDataSourceProperties(
            PoolProperties write,
            PoolProperties read
    ) {
    }

    public record PoolProperties(
            String url,
            String username,
            String password,
            int maxPoolSize,
            int minIdle
    ) {
        public String poolName() {
            String value = url == null ? "undefined" : url;
            return "db-pool-" + Integer.toHexString(value.hashCode());
        }
    }

    private static class FallbackDataSource extends org.springframework.jdbc.datasource.AbstractDataSource {
        private final DataSource primary;
        private final DataSource fallback;

        private FallbackDataSource(DataSource primary, DataSource fallback) {
            this.primary = primary;
            this.fallback = fallback;
        }

        @Override
        public Connection getConnection() throws SQLException {
            try {
                return primary.getConnection();
            } catch (SQLException ex) {
                return fallback.getConnection();
            }
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            try {
                return primary.getConnection(username, password);
            } catch (SQLException ex) {
                return fallback.getConnection(username, password);
            }
        }
    }
}

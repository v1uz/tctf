package ru.capybarovsk.overhaul.database;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PropertyDataSourceConfig {
    private final DataSourceProperties props;

    public PropertyDataSourceConfig(DataSourceProperties dataSourceProperties) {
        this.props = dataSourceProperties;
    }

    @Bean
    public DataSource defaultDataSource() {
        return DataSourceBuilder.create()
                .url(String.format("jdbc:postgresql://%s:%d/%s?tcpKeepAlive=true", props.host(), props.port(),
                        props.name()))
                .username(props.username())
                .password(props.password())
                .build();
    }
}

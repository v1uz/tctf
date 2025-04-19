package ru.capybarovsk.overhaul.database;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("database")
public record DataSourceProperties(
        String host,
        int port,
        String name,
        String username,
        String password
) {
    @ConstructorBinding
    public DataSourceProperties {}
}

package org.cloudfoundry.samples.music.config.data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Profile("postgres-local")
@ConfigurationProperties(prefix = "spring.datasource")
public class PostgresLocalDataSourceConfig extends AbstractLocalDataSourceConfig {

    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;

    @Bean
    public DataSource dataSource() {
        System.out.println("URL injected is " + url);
        return createDataSource(this.url, "org.postgresql.Driver", this.username, this.password);
    }

}

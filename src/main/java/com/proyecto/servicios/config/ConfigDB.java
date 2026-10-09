package com.proyecto.servicios.config;

import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {
                "com.proyecto.servicios.repositorys.sf",
                "com.proyecto.servicios.repositorys.gestopago"
        },
        transactionManagerRef = "sfTransactionManager",
        entityManagerFactoryRef = "sfEntityManagerFactory")
public class ConfigDB {
    private final Environment env;

    @Bean(name = "sfDatasource")
    public DataSource sfDatasource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(env.getRequiredProperty("spring.datasource.url"));
        config.setUsername(env.getRequiredProperty("spring.datasource.username"));
        config.setPassword(env.getRequiredProperty("spring.datasource.password"));
        config.setMaximumPoolSize(env.getProperty(
                "spring.datasource.hikari.maximum-pool-size", Integer.class, 10));
        config.setConnectionTimeout(env.getProperty(
                "spring.datasource.hikari.connection-timeout", Long.class, 5000L));
        config.setMaxLifetime(env.getProperty(
                "spring.datasource.hikari.max-lifetime", Long.class, 1800000L));
        config.setPoolName("sfDatasource");
        return new HikariDataSource(config);
    }

    @Bean(name = "sfEntityManagerFactory")
    @DependsOn("flyway")
    public LocalContainerEntityManagerFactoryBean sfEntityManagerFactory(
            @Qualifier("sfDatasource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factory =
                new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan(
                "com.proyecto.servicios.entity.sf",
                "com.proyecto.servicios.entity.gestopago");
        factory.setPersistenceUnitName("sfDatasource");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setJpaPropertyMap(Map.of(
                "hibernate.hbm2ddl.auto", "none",
                "hibernate.show_sql", "false",
                "hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect"));
        return factory;
    }

    @Bean(name = "sfTransactionManager")
    public PlatformTransactionManager sfTransactionManager(
            @Qualifier("sfEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager manager = new JpaTransactionManager(entityManagerFactory);
        manager.setDefaultTimeout(env.getProperty(
                "spring.transaction.default-timeout", Integer.class, 30));
        return manager;
    }
}

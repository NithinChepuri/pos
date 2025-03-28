package com.increff.spring;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class AppProperties {

    // Database properties
    @Value("${jdbc.driverClassName}")
    private String jdbcDriverClassName;

    @Value("${jdbc.url}")
    private String jdbcUrl;

    @Value("${jdbc.username}")
    private String jdbcUsername;

    @Value("${jdbc.password}")
    private String jdbcPassword;

    // Connection pool properties
    @Value("${connection.pool.initialSize}")
    private int connectionPoolInitialSize;

    @Value("${connection.pool.maxSize}")
    private int connectionPoolMaxSize;

    @Value("${connection.pool.minSize}")
    private int connectionPoolMinSize;

    @Value("${connection.pool.maxIdleTime}")
    private int connectionPoolMaxIdleTime;

    // Hibernate properties
    @Value("${hibernate.dialect}")
    private String hibernateDialect;

    @Value("${invoice.service.url}")
    private String invoiceServiceUrl;
    
    } 
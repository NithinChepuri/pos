package com.increff.spring;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
public class DbConfig {

    @Autowired
    private AppProperties appProperties;

    @Bean
    public DataSource dataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(appProperties.getJdbcDriverClassName());
        dataSource.setUrl(appProperties.getJdbcUrl());
        dataSource.setUsername(appProperties.getJdbcUsername());
        dataSource.setPassword(appProperties.getJdbcPassword());
        dataSource.setInitialSize(appProperties.getConnectionPoolInitialSize());
        dataSource.setMaxActive(appProperties.getConnectionPoolMaxSize());
        dataSource.setMinIdle(appProperties.getConnectionPoolMinSize());
        dataSource.setMaxIdle(appProperties.getConnectionPoolMaxIdleTime());
        dataSource.setDefaultAutoCommit(true);
        dataSource.setTestOnBorrow(true);
        dataSource.setValidationQuery("SELECT 1");
        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.increff.entity");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(true);
        vendorAdapter.setGenerateDdl(true);
        em.setJpaVendorAdapter(vendorAdapter);
        
        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", appProperties.getHibernateDialect());
        properties.setProperty("hibernate.show_sql", appProperties.getHibernateShowSql());
        properties.setProperty("hibernate.format_sql", appProperties.getHibernateFormatSql());
        properties.setProperty("hibernate.hbm2ddl.auto", appProperties.getHibernateHbm2ddlAuto());
        properties.setProperty("hibernate.connection.autocommit", "true");
        properties.setProperty("hibernate.jdbc.batch_size", appProperties.getHibernateJdbcBatchSize());
        properties.setProperty("hibernate.order_updates", appProperties.getHibernateOrderUpdates());
        properties.setProperty("hibernate.connection.release_mode", appProperties.getHibernateConnectionReleaseMode());
        em.setJpaProperties(properties);
        
        return em;
    }

    @Bean
    public JpaTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        transactionManager.setDefaultTimeout(30);
        return transactionManager;
    }
} 
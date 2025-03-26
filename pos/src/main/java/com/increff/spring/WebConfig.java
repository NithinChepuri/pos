package com.increff.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {
    
    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);
    
    @Autowired
    private SecurityInterceptor securityInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        logger.info("Registering security interceptor for all API paths");
        registry.addInterceptor(securityInterceptor)
            .addPathPatterns("/api/**", "/swagger-ui.html", "/swagger-ui/**", "/v2/api-docs/**", "/webjars/**", "/swagger-resources/**")
            .excludePathPatterns("/api/auth/**");
    }
} 
package com.increff.spring;

import com.increff.model.enums.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import javax.servlet.ServletException;
import java.util.Arrays;
import java.util.Collections;
import javax.servlet.FilterConfig;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .cors().and()
            .csrf().disable()
            .authorizeRequests()
            .antMatchers("/api/auth/**").permitAll()
            .antMatchers(HttpMethod.POST, "/api/*/upload").permitAll()
            .antMatchers(HttpMethod.POST, "/api/inventory/**").hasRole("SUPERVISOR")
            .antMatchers(HttpMethod.PUT, "/api/inventory/**").hasRole("SUPERVISOR")
            .antMatchers(HttpMethod.DELETE, "/api/inventory/**").hasRole("SUPERVISOR")
            .antMatchers(HttpMethod.POST, "/api/products/**").hasRole("SUPERVISOR")
            .antMatchers(HttpMethod.PUT, "/api/products/**").hasRole("SUPERVISOR")
            .antMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("SUPERVISOR")
            .anyRequest().authenticated()
            .and()
            .addFilterBefore(sessionTimeoutFilter(), UsernamePasswordAuthenticationFilter.class)
            .sessionManagement()
            .maximumSessions(1)
            .expiredUrl("/api/auth/login");
    }

    @Bean
    public Filter sessionTimeoutFilter() {
        return new Filter() {
            @Override
            public void init(FilterConfig filterConfig) throws ServletException {
                // Not needed, but must be implemented
            }

            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
                    throws IOException, ServletException {
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                HttpSession session = httpRequest.getSession(false);

                if (session != null) {
                    Long lastChecked = (Long) session.getAttribute("lastCheckedTime");
                    long currentTime = System.currentTimeMillis();
                    
                    if (lastChecked != null && currentTime - lastChecked > 300000) { // 5 minutes
                        session.invalidate();
                        ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session expired");
                        return;
                    }
                    session.setAttribute("lastCheckedTime", currentTime);
                }
                
                chain.doFilter(request, response);
            }

            @Override
            public void destroy() {
                // Not needed, but must be implemented
            }
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
        configuration.setExposedHeaders(Arrays.asList("x-auth-token"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setDefaultEncoding("utf-8");
        resolver.setMaxUploadSize(5242880); // 5MB
        return resolver;
    }
}
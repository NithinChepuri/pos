package com.increff.spring;

import com.increff.model.enums.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Component
@Order(1)
public class SecurityFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Initializing SecurityFilter");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        String queryString = httpRequest.getQueryString();
        
        logger.info("Processing request: {} {} (QueryString: {})", method, path, queryString);
        logger.info("Request Headers:");
        java.util.Enumeration<String> headerNames = httpRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            logger.info("{}: {}", headerName, httpRequest.getHeader(headerName));
        }

        // Handle CORS preflight requests
        if ("OPTIONS".equals(method)) {
            logger.info("Handling OPTIONS request");
            httpResponse.setHeader("Access-Control-Allow-Origin", "*");
            httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            httpResponse.setHeader("Access-Control-Allow-Headers", "authorization, content-type, x-auth-token");
            httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
            httpResponse.setHeader("Access-Control-Max-Age", "3600");
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // Set CORS headers for all responses
        httpResponse.setHeader("Access-Control-Allow-Origin", "*");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");

        // Skip authentication for auth endpoints and public resources
        if (path.startsWith("/api/auth/") || 
            path.startsWith("/employee/api/auth/") ||
            path.startsWith("/swagger-ui") || 
            path.startsWith("/v2/api-docs") || 
            path.startsWith("/webjars/") ||
            path.startsWith("/swagger-resources")) {
            logger.info("Skipping authentication for path: {}", path);
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);

        // Check if user is logged in
        if (session == null || session.getAttribute("userId") == null) {
            logger.warn("Unauthorized access attempt to: {} {}", method, path);
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Check last authentication time
        Long lastChecked = (Long) session.getAttribute("lastCheckedTime");
        long currentTime = System.currentTimeMillis();

        if (lastChecked != null && currentTime - lastChecked > 300000) { // 5 minutes
            logger.info("Session expired for user");
            session.invalidate();
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        session.setAttribute("lastCheckedTime", currentTime);

        // Role-based access control
        Role role = (Role) session.getAttribute("role");

        if (role == Role.OPERATOR) {
            // Allow search operations (POST requests with /search in the path)
            if (method.equals("POST") && (path.contains("/search"))||path.contains("/filter")) {
                chain.doFilter(request, response);
                return;
            }

            // Block specific operations for operators
            if ((path.contains("/inventory") || path.contains("/products") || path.contains("/clients")) 
                && (method.equals("POST") || method.equals("PUT"))) {
                logger.warn("Access denied: Operator attempted to modify restricted resource: {} {}", method, path);
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        logger.info("Destroying SecurityFilter");
    }
} 
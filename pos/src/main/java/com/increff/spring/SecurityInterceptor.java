package com.increff.spring;

import com.increff.model.enums.Role;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class SecurityInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        logger.debug("Processing request: {} {}", method, path);
        
        // Skip OPTIONS requests (CORS preflight)
        if ("OPTIONS".equals(method)) {
            return true;
        }
        
        // Skip authentication for auth endpoints
        if (path.startsWith("/api/auth/")) {
            return true;
        }
        
        HttpSession session = request.getSession(false);
        
        // Check if user is logged in
        if (session == null || session.getAttribute("userId") == null) {
            logger.warn("Unauthorized access attempt to: {} {}", method, path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        
        // Check last authentication time
        Long lastChecked = (Long) session.getAttribute("lastCheckedTime");
        long currentTime = System.currentTimeMillis();
        
        if (lastChecked != null && currentTime - lastChecked > 300000) { // 5 minutes
            logger.info("Session expired for user");
            session.invalidate();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        
        session.setAttribute("lastCheckedTime", currentTime);
        
        // Role-based access control
        Role role = (Role) session.getAttribute("role");
        
        if (role == Role.OPERATOR) {
            // Block supervisor-only operations
            if ((path.contains("/inventory") && !method.equals("GET")) ||
                (path.contains("/products") && !method.equals("GET")) ||
                (path.contains("/clients") && !method.equals("GET"))) {
                logger.warn("Access denied: Operator attempted to access supervisor resource: {} {}", method, path);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }
        }
        
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, 
            Object handler, ModelAndView modelAndView) throws Exception {
        // Not needed, but must be implemented
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
            Object handler, Exception ex) throws Exception {
        // Not needed, but must be implemented
    }
} 
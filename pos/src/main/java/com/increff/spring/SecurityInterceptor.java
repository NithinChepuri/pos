package com.increff.spring;

import com.increff.entity.UserEntity.Role;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class SecurityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession();
        
        // Check if user is logged in
        if (session.getAttribute("userId") == null) {
            response.setStatus(401);
            return false;
        }
        
        // Check last authentication time
        Long lastChecked = (Long) session.getAttribute("lastCheckedTime");
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastChecked > 300000) { // 5 minutes
            session.setAttribute("lastCheckedTime", currentTime);
        }
        
        // Role-based access control
        String path = request.getRequestURI();
        String method = request.getMethod();
        Role role = (Role) session.getAttribute("role");
        
        if (role == Role.OPERATOR) {
            // Block only upload/edit operations
            if (path.contains("/upload") || 
                (path.contains("/inventory") && !method.equals("GET")) ||
                (path.contains("/products") && !method.equals("GET"))) {
                response.setStatus(403);
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
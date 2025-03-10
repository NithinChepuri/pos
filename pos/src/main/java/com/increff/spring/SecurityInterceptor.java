package com.increff.spring;

import com.increff.entity.UserEntity.Role;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class SecurityInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Skip auth check for OPTIONS requests (CORS preflight)
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }

        // Skip auth check for login/signup endpoints
        if (request.getRequestURI().startsWith("/api/auth/")) {
            return true;
        }

        HttpSession session = request.getSession();
        if (session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // For supervisor-only endpoints
        if (request.getRequestURI().startsWith("/api/admin/")) {
            String role = (String) session.getAttribute("role");
            if (!"SUPERVISOR".equals(role)) {
                response.sendError(HttpStatus.FORBIDDEN.value(), "Access denied");
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
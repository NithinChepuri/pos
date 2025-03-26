package com.increff.util;

import com.increff.model.enums.Role;
import com.increff.service.ApiException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class AuthorizationUtil {

    /**
     * Check if the current user has supervisor access
     * @throws ApiException if user doesn't have supervisor role
     */
    public static void checkSupervisorAccess(HttpServletRequest request) throws ApiException {
        HttpSession session = request.getSession();
        Role role = (Role) session.getAttribute("role");
        
        if (role != Role.SUPERVISOR) {
            throw new ApiException("Access denied. Supervisor role required.");
        }
    }
} 
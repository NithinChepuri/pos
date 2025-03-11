package com.increff.entity;

public enum OrderStatus {
    CREATED,    // Initial state when order is created
    INVOICED,   // When invoice is generated
    CANCELLED   // When order is cancelled
}
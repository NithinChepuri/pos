package com.increff.model;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

public class SalesReportForm {
    @NotNull(message = "Start date is required")
    private ZonedDateTime startDate;
    
    @NotNull(message = "End date is required")
    private ZonedDateTime endDate;
    
    private Long clientId;
    
    // Getters and Setters
    public ZonedDateTime getStartDate() { return startDate; }
    public void setStartDate(ZonedDateTime startDate) { this.startDate = startDate; }
    
    public ZonedDateTime getEndDate() { return endDate; }
    public void setEndDate(ZonedDateTime endDate) { this.endDate = endDate; }
    
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
} 
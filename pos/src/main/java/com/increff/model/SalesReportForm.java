package com.increff.model;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class SalesReportForm {
    @NotNull(message = "Start date is required")
    private ZonedDateTime startDate;
    
    @NotNull(message = "End date is required")
    private ZonedDateTime endDate;
    
    private Long clientId;
} 
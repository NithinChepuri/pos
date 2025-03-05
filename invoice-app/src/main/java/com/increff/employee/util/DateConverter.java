package com.increff.employee.util;

import java.time.LocalDateTime;
import java.util.Map;

public class DateConverter {
    public static LocalDateTime convertToLocalDateTime(Map<String, Object> dateMap) {
        int year = (Integer) dateMap.get("year");
        int month = (Integer) dateMap.get("monthValue");
        int day = (Integer) dateMap.get("dayOfMonth");
        int hour = (Integer) dateMap.get("hour");
        int minute = (Integer) dateMap.get("minute");
        int second = (Integer) dateMap.get("second");
        
        return LocalDateTime.of(year, month, day, hour, minute, second);
    }
} 
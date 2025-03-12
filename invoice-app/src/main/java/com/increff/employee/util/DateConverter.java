package com.increff.employee.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

public class DateConverter {
    public static LocalDateTime convertToLocalDateTime(Map<String, Object> dateMap) {
        if (dateMap == null) {
            return null;
        }
        
        Integer year = (Integer) dateMap.get("year");
        Integer month = (Integer) dateMap.get("monthValue");
        Integer day = (Integer) dateMap.get("dayOfMonth");
        Integer hour = (Integer) dateMap.get("hour");
        Integer minute = (Integer) dateMap.get("minute");
        Integer second = (Integer) dateMap.get("second");
        
        return LocalDateTime.of(year, month, day, hour, minute, second);
    }
    
    public static ZonedDateTime convertToZonedDateTime(Map<String, Object> dateMap) {
        if (dateMap == null) {
            return null;
        }
        
        Integer year = (Integer) dateMap.get("year");
        Integer month = (Integer) dateMap.get("monthValue");
        Integer day = (Integer) dateMap.get("dayOfMonth");
        Integer hour = (Integer) dateMap.get("hour");
        Integer minute = (Integer) dateMap.get("minute");
        Integer second = (Integer) dateMap.get("second");
        
        // Get zone information
        Map<String, Object> zoneMap = (Map<String, Object>) dateMap.get("zone");
        String zoneId = (String) zoneMap.get("id");
        
        return ZonedDateTime.of(year, month, day, hour, minute, second, 0, ZoneId.of(zoneId));
    }
} 
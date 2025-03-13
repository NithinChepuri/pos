package com.increff.service;

import com.increff.dao.ReportDao;
import com.increff.model.SalesReportData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private ReportDao reportDao;

    @Transactional(readOnly = true)
    public List<SalesReportData> getSalesReport(ZonedDateTime startDate, ZonedDateTime endDate) {
        return reportDao.getSalesReport(startDate, endDate);
    }
} 
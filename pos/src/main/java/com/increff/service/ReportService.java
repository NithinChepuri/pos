package com.increff.service;

import com.increff.model.SalesReportData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class ReportService {

    @PersistenceContext
    private EntityManager em;

    @Transactional(readOnly = true)
    public List<SalesReportData> getSalesReport(ZonedDateTime startDate, ZonedDateTime endDate) {
        String queryStr = 
            "SELECT NEW com.increff.model.SalesReportData(" +
            "p.barcode, " +        // barcode
            "p.name, " +           // product name
            "SUM(oi.quantity), " + // total quantity
            "SUM(oi.quantity * oi.sellingPrice)) " + // total revenue
            "FROM OrderEntity o, OrderItemEntity oi, ProductEntity p " +
            "WHERE o.id = oi.orderId " +
            "AND oi.productId = p.id " +
            "AND o.status = 'INVOICED' " +
            "AND o.createdAt >= :startDate " +
            "AND o.createdAt < :endDate " +
            "GROUP BY p.barcode, p.name " +
            "ORDER BY p.name";

        TypedQuery<SalesReportData> query = em.createQuery(queryStr, SalesReportData.class)
            .setParameter("startDate", startDate)
            .setParameter("endDate", endDate);

        return query.getResultList();
    }
} 
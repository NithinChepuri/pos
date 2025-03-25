package com.increff.dao;

import com.increff.model.SalesReportData;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.time.ZonedDateTime;
import java.util.List;

@Repository
public class ReportDao extends AbstractDao {
    
    private static final String SALES_REPORT_BASE_QUERY = 
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
        "AND o.createdAt < :endDate ";
    
    private static final String CLIENT_FILTER = "AND p.clientId = :clientId ";
    
    private static final String GROUP_AND_ORDER = 
        "GROUP BY p.barcode, p.name " +
        "ORDER BY p.name";
    
    public List<SalesReportData> getSalesReport(ZonedDateTime startDate, ZonedDateTime endDate, Long clientId) {
        StringBuilder queryStr = new StringBuilder(SALES_REPORT_BASE_QUERY);
            
        // Add client filter if clientId is provided
        if (clientId != null) {
            queryStr.append(CLIENT_FILTER);
        }
            
        queryStr.append(GROUP_AND_ORDER);

        TypedQuery<SalesReportData> query = em.createQuery(queryStr.toString(), SalesReportData.class)
            .setParameter("startDate", startDate)
            .setParameter("endDate", endDate);
            
        // Set clientId parameter if provided
        if (clientId != null) {
            query.setParameter("clientId", clientId);
        }

        return query.getResultList();
    }
} 